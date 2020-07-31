package cn.troph.tomon.ui.chat.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.*
import cn.troph.tomon.core.structures.*
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.AppUIEvent
import cn.troph.tomon.ui.states.AppUIEventType
import cn.troph.tomon.ui.states.ChannelSelection
import cn.troph.tomon.ui.widgets.UserAvatar
import com.nex3z.flowlayout.FlowLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.widget_guild_channel_selector_item.view.*
import java.lang.Exception

class GuildChannelSelectorAdapter : RecyclerView.Adapter<GuildChannelSelectorAdapter.ViewHolder>() {

    var onItemClickListner: OnVoiceChannelClick? = null

    companion object {
        const val TYPE_CHANNEL = 0
        const val TYPE_CATEGORY = 1
        const val TYPE_EMPTY = 2
    }

    class ViewHolder(itemView: View, private val onItemHolderClickListener: OnVoiceChannelClick?) :
        RecyclerView.ViewHolder(itemView) {
        private var text: TextView = itemView.findViewById(R.id.text_name)
        private var image: ImageView = itemView.findViewById(R.id.image_icon)
        private var voiceUserContainerLayout: FlowLayout =
            itemView.findViewById(R.id.user_avatar_flow_ll)
        var disposable: Disposable? = null
        var channel: GuildChannel? = null

        fun bind(channel: GuildChannel) {
            itemView.isActivated = AppState.global.channelSelection.value.channelId == channel.id
            when (channel.type) {
                ChannelType.TEXT -> {
                    if (channel.isPrivate) {
                        image.setImageResource(R.drawable.ic_channel_text_lock)
                    } else {
                        image.setImageResource(R.drawable.ic_channel_text_unlock)
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
            if (channel is TextChannel) {
                if (channel.unread) {
                    text.typeface = Typeface.DEFAULT_BOLD
                    text.setTextColor(Color.parseColor("#FFFFFF"))
                } else {
                    text.typeface = Typeface.DEFAULT
                    text.setTextColor(Color.parseColor("#969696"))
                }
                if (channel.mention != 0) {
                    itemView.channel_unread_mention_notification.text = channel.mention.toString()
                    itemView.channel_unread_mention_notification.visibility = View.VISIBLE
                } else {
                    itemView.channel_unread_mention_notification.visibility = View.GONE
                }
            } else if (channel is VoiceChannel) {
                itemView.channel_unread_mention_notification.visibility = View.GONE
            }

            text.text = channel.name
            if (channel is VoiceChannel) {
                if (channel.voiceStates.size > 0) {
                    for (index in 0 until voiceUserContainerLayout.childCount) {
                        val avatar = voiceUserContainerLayout[index] as UserAvatar
                        try {
                            avatar.user = Client.global.users[channel.voiceStates[index].userId]
                            avatar.visibility = View.VISIBLE
                        } catch (e: Exception) {
                            avatar.visibility = View.GONE
                        }
                    }
                    voiceUserContainerLayout.visibility = View.VISIBLE
                } else {
                    voiceUserContainerLayout.visibility = View.GONE
                }
                if (channel.voiceStates.size > 0) {
                    text.text = "${channel.name} ${channel.voiceStates.size}人"
                } else {
                    text.text = channel.name
                }
            }
            disposable?.dispose()
            disposable =
                channel.observable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    text.text = channel.name
                }
            Client.global.eventBus.observeEventOnUi<MessageCreateEvent>().subscribe(Consumer {
                if (channel is TextChannel) {
                    if (channel.unread && it.message.authorId != Client.global.me.id) {
                        text.typeface = Typeface.DEFAULT_BOLD
                        text.setTextColor(Color.parseColor("#FFFFFF"))
                    } else {
                        text.typeface = Typeface.DEFAULT
                        text.setTextColor(Color.parseColor("#969696"))
                    }

                }

            })
            Client.global.eventBus.observeEventOnUi<MessageAtMeEvent>().subscribe(Consumer {
                if (channel is TextChannel && it.message.channel?.id == channel.id && it.message.guild?.id == AppState.global.channelSelection.value.guildId) {
                    itemView.channel_unread_mention_notification.text = channel.mention.toString()
                    itemView.channel_unread_mention_notification.visibility = View.VISIBLE
                }
            })
            Client.global.eventBus.observeEventOnUi<MessageReadEvent>().subscribe(Consumer {

                if (channel is TextChannel && it.message.channelId == channel.id) {
                    text.typeface = Typeface.DEFAULT
                    text.setTextColor(Color.parseColor("#969696"))
                    itemView.channel_unread_mention_notification.visibility = View.GONE
                }
            })
            Client.global.eventBus.observeEventOnUi<GuildVoiceSelectorEvent>().subscribe(Consumer {
                if (channel is VoiceChannel) {
                    if (channel.id == it.channelId) {
                        text.typeface = Typeface.DEFAULT_BOLD
                        text.setTextColor(Color.parseColor("#FFFFFF"))
                    } else {
                        text.typeface = Typeface.DEFAULT
                        text.setTextColor(Color.parseColor("#969696"))
                    }
                }
            })
            Client.global.eventBus.observeEventOnUi<VoiceStateUpdateEvent>()
                .subscribe(Consumer { event ->
                    if (channel is VoiceChannel) {
                        if (!event.voiceUpdate.channelId.isNullOrEmpty() && channel.id == event.voiceUpdate.channelId && channel.guildId == event.voiceUpdate.guildId) {//加入
                            val user = channel.voiceStates.find {
                                it.userId == event.voiceUpdate.userId
                            }
                            if (user == null) {
                                channel.voiceStates.add(event.voiceUpdate)
                            }
                        } else {//删除
                            channel.voiceStates.removeIf {
                                it.userId == event.voiceUpdate.userId
                            }
                        }
                        //update UI
                        if (channel.voiceStates.size > 0) {
                            for (index in 0 until voiceUserContainerLayout.childCount) {
                                val avatar = voiceUserContainerLayout[index] as UserAvatar
                                try {
                                    avatar.visibility = View.VISIBLE
                                    avatar.user =
                                        Client.global.users[channel.voiceStates[index].userId]
                                } catch (e: Exception) {
                                    avatar.visibility = View.GONE
                                }
                            }
                            voiceUserContainerLayout.visibility = View.VISIBLE
                            text.text = "${channel.name} ${channel.voiceStates.size}人"
                        } else {
                            text.text = channel.name
                            voiceUserContainerLayout.visibility = View.GONE
                        }
                    }
                })


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
                        AppState.global.eventBus.postEvent(
                            AppUIEvent(
                                AppUIEventType.CHANNEL_DRAWER,
                                false
                            )
                        )
                    }
                    ChannelType.VOICE -> {
                        onItemHolderClickListener?.let {
                            it.onVoiceChannelSelected(channel)
                        }
                    }
                }

            }
        }
    }

    var guildId: String? = null
        set(value) {
            field = value
            val channels = value?.let { Client.global.guilds[value]?.channels }
            disposable?.dispose()
            disposable =
                channels?.observable?.observeOn(AndroidSchedulers.mainThread())?.subscribe {
                    list = updateVisibility()
                }
        }

    var disposable: Disposable? = null

    var list: List<String> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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
        return ViewHolder(inflatedView, onItemClickListner)
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

interface OnVoiceChannelClick {
    fun onVoiceChannelSelected(channel: GuildChannel)
}