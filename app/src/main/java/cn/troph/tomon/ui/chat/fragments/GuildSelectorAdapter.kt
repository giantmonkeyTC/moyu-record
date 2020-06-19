package cn.troph.tomon.ui.chat.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.events.MessageReadEvent
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import cn.troph.tomon.ui.widgets.GuildAvatar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.widget_guild_selector_item.view.*
import kotlinx.coroutines.flow.channelFlow


class GuildSelectorAdapter(private val guildList: MutableList<Guild>) :
    RecyclerView.Adapter<GuildSelectorAdapter.ViewHolder>() {
    private lateinit var mOnItemClickListener: OnItemClickListener

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = onItemClickListener
    }

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
            if (guild.mention != 0) {
                if (guild.mention > 99)
                    itemView.guild_unread_mention_notification.text = "···"
                else
                    itemView.guild_unread_mention_notification.text = guild.mention.toString()
                itemView.guild_unread_mention_notification.visibility = View.VISIBLE
            } else
                itemView.guild_unread_mention_notification.visibility = View.GONE
            if (guild.unread)
                itemView.guild_unread_message_notification.visibility = View.VISIBLE
            else
                itemView.guild_unread_message_notification.visibility = View.GONE
            avatar.selecting = AppState.global.channelSelection.value.guildId == guild.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView =
            layoutInflater.inflate(R.layout.widget_guild_selector_item, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int = guildList.size

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val list = Client.global.guilds.list
//        val guild = if (position >= 0 && position < list.size) list[position] else null
//
//        if (guild != null) {
//            holder.bind(guild)
//        }
        holder.itemView.setOnClickListener {
            val old = AppState.global.channelSelection.value
            AppState.global.channelSelection.value =
                ChannelSelection(guildId = guildList[position].id, channelId = old.channelId)
            val position = holder.layoutPosition
            mOnItemClickListener.onItemClick(holder.itemView, position)
        }
            holder.bind(guildList[position])


    }

}