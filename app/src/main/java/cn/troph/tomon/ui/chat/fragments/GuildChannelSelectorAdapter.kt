package cn.troph.tomon.ui.chat.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.Event
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.ui.states.AppState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class GuildChannelSelectorAdapter : RecyclerView.Adapter<GuildChannelSelectorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private var text: TextView = itemView.findViewById(R.id.text_name)

        fun bind(channel: GuildChannel) {
            text.text = channel.name
        }
    }

    var guildId: String? = null
        set(value) {
            field = value
            observable = if (value != null) {
                Observable.create(Client.global.guilds[guildId!!]?.channels)
            } else {
                null
            }
            observable?.observeOn(AndroidSchedulers.mainThread())?.subscribe {
                notifyDataSetChanged()
            }
        }

    private var observable: Observable<Event<GuildChannel>>? = null

    init {
        guildId = AppState.global.channelSelection.value.guildId
        println(guildId)
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                guildId = it.guildId
                if (guildId != null) {
                    println(Client.global.guilds[guildId!!])
                    println(Client.global.guilds[guildId!!]?.channels?.list?.size)
                }
                notifyDataSetChanged()
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView =
            layoutInflater.inflate(R.layout.widget_guild_channel_selector_item, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int =
        if (guildId != null) Client.global.guilds[guildId!!]?.channels?.list?.size ?: 0 else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (guildId == null) {
            return
        }
        val list = Client.global.guilds[guildId!!]?.channels?.list ?: return
        val channel = if (position >= 0 && position < list.size) list[position] else null
        if (channel != null) {
            holder.bind(channel)
        }
    }
}