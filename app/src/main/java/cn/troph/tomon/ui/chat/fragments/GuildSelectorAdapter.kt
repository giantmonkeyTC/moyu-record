package cn.troph.tomon.ui.chat.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.widget_guild_selector_item.view.*


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
            AppState.global.channelSelection.value = ChannelSelection(
                guildId = guildList[holder.adapterPosition].id,
                channelId = null
            )
        }
        holder.itemView.view_avatar.guild = guildList[position]

        val guild = guildList[position]
        holder.itemView.guild_voice_indicator.visibility = if (guild.isVoiceChatting) View.VISIBLE else View.GONE
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
        if (holder.itemView.guild_indicator.isVisible) {
            holder.itemView.guild_indicator.startAnimation(
                AnimationUtils.loadAnimation(
                    holder.itemView.context,
                    R.anim.scale
                )
            )
        }
    }

}