package cn.troph.tomon.ui.chat.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import cn.troph.tomon.ui.widgets.GuildAvatar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class GuildSelectorAdapter : RecyclerView.Adapter<GuildSelectorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private var avatar: GuildAvatar = itemView.findViewById(R.id.view_avatar)
        private var guild: Guild? = null

        init {
            AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                avatar.selecting = AppState.global.channelSelection.value.guildId == guild?.id
            }
        }

        fun bind(guild: Guild) {
            this.guild = guild
            avatar.url = guild.iconURL
            avatar.name = guild.name
            avatar.selecting = AppState.global.channelSelection.value.guildId == guild.id
            itemView.setOnClickListener {
                val old = AppState.global.channelSelection.value
                AppState.global.channelSelection.value =
                    ChannelSelection(guildId = guild.id, channelId = old.channelId)
            }
        }
    }

    init {
        Observable.create(Client.global.guilds).observeOn(AndroidSchedulers.mainThread())
            .subscribe { _ ->
                notifyDataSetChanged()
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView =
            layoutInflater.inflate(R.layout.widget_guild_selector_item, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int = Client.global.guilds.list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = Client.global.guilds.list
        val guild = if (position >= 0 && position < list.size) list[position] else null
        if (guild != null) {
            holder.bind(guild)
        }
    }


}