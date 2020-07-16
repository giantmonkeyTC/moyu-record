package cn.troph.tomon.ui.chat.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
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
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.widget_guild_selector_item.view.*
import kotlinx.coroutines.flow.channelFlow


class GuildSelectorAdapter(private val guildList: MutableList<Guild>) :
    RecyclerView.Adapter<GuildSelectorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            layoutInflater.inflate(
                R.layout.widget_guild_selector_item,
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int = guildList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            Logger.d("guild size:${guildList.size} adapter:${holder.adapterPosition}")
            val old = AppState.global.channelSelection.value
            AppState.global.channelSelection.value = ChannelSelection(
                guildId = guildList[holder.adapterPosition].id,
                channelId = old.channelId
            )
            holder.itemView.guild_indicator.startAnimation(
                AnimationUtils.loadAnimation(
                    holder.itemView.context,
                    R.anim.scale
                )
            )

            (holder.itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                .apply {
                    replace(R.id.fragment_guild_channels, GuildChannelSelectorFragment())
                    addToBackStack(null)
                }.commit()


        }
        holder.itemView.view_avatar.guild = guildList[position]
        val guild = guildList[position]
        if (guild.mention != 0) {
            if (guild.mention > 99)
                holder.itemView.guild_unread_mention_notification.text = "···"
            else
                holder.itemView.guild_unread_mention_notification.text = guild.mention.toString()
            holder.itemView.guild_unread_mention_notification.visibility = View.VISIBLE
        } else
            holder.itemView.guild_unread_mention_notification.visibility = View.GONE
        if (guild.unread)
            holder.itemView.guild_unread_message_notification.visibility = View.VISIBLE
        else
            holder.itemView.guild_unread_message_notification.visibility = View.GONE
        holder.itemView.view_avatar.selecting = guildList[position].isSelected
        holder.itemView.guild_indicator.visibility =
            if (guild.isSelected) View.VISIBLE else View.INVISIBLE
    }

}