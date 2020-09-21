package cn.troph.tomon.ui.chat.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.User
import cn.troph.tomon.ui.activities.ChannelInfoActivity
import cn.troph.tomon.ui.activities.ChatActivity
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.AppUIEvent
import cn.troph.tomon.ui.states.AppUIEventType
import cn.troph.tomon.ui.states.ChannelSelection
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.widget_dmchannel_item.view.*
import kotlinx.android.synthetic.main.widget_member_item.view.*

class DmChannelSelectorAdapter(private val dmChannelList: MutableList<DmChannel>) :
    RecyclerView.Adapter<DmChannelSelectorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DmChannelSelectorAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.widget_dmchannel_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dmChannelList.size
    }

    private fun bind(itemView: View, dmChannel: DmChannel) {
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, ChatActivity::class.java)
            val bundle = Bundle()
            bundle.putString("guildId", "@me")
            bundle.putString("channelId", dmChannel.id)
            intent.putExtras(bundle)
            startActivity(
                itemView.context,
                intent,
                ActivityOptions.makeCustomAnimation(
                    itemView.context,
                    R.anim.slide_in_right_custom,
                    R.anim.no_animation
                ).toBundle()
            )
            AppState.global.channelSelection.value =
                ChannelSelection(guildId = "@me", channelId = dmChannel.id)
        }
        itemView.dmchannel_user_avatar.user = dmChannel.recipient
        itemView.text_name.text = dmChannel.recipient?.name
        itemView.dm_user_unread_tv.visibility =
            if (dmChannel.unReadCount > 0 && dmChannel.unread) View.VISIBLE else View.GONE
        itemView.dm_user_unread_tv.text = dmChannel.unReadCount.toString()
        dmChannel.messageNotifications
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bind(holder.itemView, dmChannelList[position])
    }
}