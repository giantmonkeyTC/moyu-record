package cn.troph.tomon.ui.chat.fragments

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.Event
import cn.troph.tomon.core.structures.CategoryChannel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

class GuildChannelSelectorAdapter : RecyclerView.Adapter<GuildChannelSelectorAdapter.ViewHolder>() {

    companion object {
        const val TYPE_CHANNEL = 0
        const val TYPE_CATEGORY = 1
        const val TYPE_EMPTY = 2
    }


    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private var text: TextView = itemView.findViewById(R.id.text_name)
        private var image: ImageView = itemView.findViewById(R.id.image_icon)
        var disposable: Disposable? = null
        var channel: GuildChannel? = null

        fun bind(channel: GuildChannel) {
            println("on root bind")
            itemView.isActivated = AppState.global.channelSelection.value.channelId == channel.id
            when (channel.type) {
                ChannelType.TEXT -> {
                    if (channel.isPrivate) {
                        image.setImageResource(R.drawable.ic_channel_text_lock)
                    } else {
                        image.setImageResource(R.drawable.ic_channel_text)
                    }
                }
                ChannelType.VOICE -> {
                    if (channel.isPrivate) {
                        image.setImageResource(R.drawable.ic_channel_voice_lock)
                    } else {
                        image.setImageResource(R.drawable.ic_channel_voice)
                    }
                }
                ChannelType.CATEGORY -> {
                    val collapse = AppState.global.channelIsCollapsed(channel.id)
                    if (collapse) {
                        image.setImageResource(R.drawable.ic_category_folding)
                    } else {
                        image.setImageResource(R.drawable.ic_category_unfolding)
                    }
                }
                else -> {
                    image.setImageDrawable(null)
                }
            }
            val r = itemView.context.resources
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16.0f + channel.indent * 16.0f,
                r.displayMetrics
            )
            val lp = image.layoutParams as ConstraintLayout.LayoutParams
            lp.marginStart = px.toInt()
            val newLp = ConstraintLayout.LayoutParams(lp)
            image.layoutParams = newLp

            text.text = channel.name
            disposable?.dispose()
            disposable =
                Observable.create(channel).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    text.text = channel.name
                }

            itemView.setOnClickListener {
                when (channel.type) {
                    ChannelType.CATEGORY -> {
                        val old = AppState.global.channelIsCollapsed(channel.id)
                        AppState.global.channelCollapse(channel.id, !old)
                    }
                    ChannelType.TEXT -> {
                        val old = AppState.global.channelSelection.value
                        AppState.global.channelSelection.value =
                            ChannelSelection(guildId = old.guildId, channelId = channel.id)
                    }
                }

            }
        }
    }

    var guildId: String? = null
        set(value) {
            field = value
            observable = if (value != null) {
                val channels = Client.global.guilds[value]?.channels
                if (channels != null) Observable.create(Client.global.guilds[value]?.channels) else null
            } else {
                null
            }
            disposable?.dispose()
            disposable = observable?.observeOn(AndroidSchedulers.mainThread())?.subscribe {
                list = updateVisibility()
            }
        }

    var disposable: Disposable? = null

    var list: List<String> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var observable: Observable<Event<GuildChannel>>? = null

    init {
        guildId = AppState.global.channelSelection.value.guildId
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                guildId = it.guildId
                list = updateVisibility()
            }
        AppState.global.channelCollapses.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                list = updateVisibility()
            }
    }

    private fun updateVisibility(): List<String> {
        if (guildId == null) {
            return emptyList()
        }
        val guild = Client.global.guilds[guildId!!] ?: return emptyList()
        val channels = guild.channels.sorted().map { it.id }
        val solidChannels = removeEmptyCategories(channels)
        val checkVisible = { id: String ->
            var visible = true
            var cursor = Client.global.channels[id] as? GuildChannel
            while (cursor != null) {
                val p = cursor!!.parent
                if (p != null && AppState.global.channelCollapses.value[p.id] == true) {
                    visible = false
                    break
                }
                cursor = p
            }
            visible
        }
        val isForceVisible = { id: String ->
            val selectedId = AppState.global.channelSelection.value.channelId
            val channel = Client.global.channels[id] as? GuildChannel
            var visible = false
            if (channel != null) {
                if (channel !is CategoryChannel && selectedId == id) {
                    visible = true
                } else if (channel is TextChannelBase && channel.unread) {
                    visible = true
                }
            }
            visible
        }
        val visibilityMap = mutableMapOf<String, Boolean>()
        for (id in solidChannels) {
            if (isForceVisible(id)) {
                var cursor = Client.global.channels[id] as? GuildChannel
                while (cursor != null) {
                    visibilityMap[cursor!!.id] = true
                    cursor = cursor!!.parent
                }
            } else {
                visibilityMap[id] = checkVisible(id)
            }
        }
        return solidChannels.filter { visibilityMap[it] ?: false }
    }

    private fun removeEmptyCategories(channels: List<String>): List<String> {
        val counters = mutableMapOf<String, Int>()
        channels.forEach {
            val channel = Client.global.channels[it] as? GuildChannel
            if (channel != null && channel !is CategoryChannel) {
                var cursor = channel
                while (cursor != null) {
                    counters[cursor.id] = (counters[cursor.id] ?: 0) + 1
                    cursor = cursor.parent
                }
            }
        }
        return channels.filter {
            counters[it] ?: 0 > 0
        }
    }

    override fun getItemViewType(position: Int): Int {
        val id = list[position]
        val channel = Client.global.channels[id] as? GuildChannel ?: return TYPE_EMPTY
        return when (channel.type) {
            ChannelType.CATEGORY -> TYPE_CATEGORY
            else -> TYPE_CHANNEL
        }
    }

    override fun getItemId(position: Int): Long {
        val id = list[position]
        return id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView = when (viewType) {
            TYPE_CHANNEL -> layoutInflater.inflate(
                R.layout.widget_guild_channel_selector_item,
                parent,
                false
            )
            TYPE_CATEGORY -> layoutInflater.inflate(
                R.layout.widget_guild_channel_category_item,
                parent,
                false
            )
            else -> View(parent.context)
        }
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val id = list[position]
        val channel = Client.global.channels[id] as? GuildChannel
        if (channel != null) {
            holder.bind(channel)
        } else {
            println("need clear or hide")
        }
    }
}