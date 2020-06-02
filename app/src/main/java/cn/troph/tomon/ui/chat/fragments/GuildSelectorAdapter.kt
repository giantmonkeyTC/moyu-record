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
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class GuildSelectorAdapter(private val guildList: MutableList<Guild>) : RecyclerView.Adapter<GuildSelectorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private var avatar: GuildAvatar = itemView.findViewById(R.id.view_avatar)
        private var guild: Guild? = null

        init {
            AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    avatar.selecting = AppState.global.channelSelection.value.guildId == guild?.id
                }
        }

        fun bind(guild: Guild) {
            this.guild = guild
            avatar.guild = guild
            avatar.selecting = AppState.global.channelSelection.value.guildId == guild.id
            itemView.setOnClickListener {
                val old = AppState.global.channelSelection.value
                AppState.global.channelSelection.value =
                    ChannelSelection(guildId = guild.id, channelId = old.channelId)
            }
        }
    }

    init {
        Client.global.guilds.observable.observeOn(AndroidSchedulers.mainThread())
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

    override fun getItemCount(): Int = guildList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val list = Client.global.guilds.list
//        val guild = if (position >= 0 && position < list.size) list[position] else null
//
//        if (guild != null) {
//            holder.bind(guild)
//        }
        holder.bind(guildList[position])
    }

}