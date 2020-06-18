package cn.troph.tomon.ui.chat.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.User
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.AppUIEvent
import cn.troph.tomon.ui.states.AppUIEventType
import cn.troph.tomon.ui.states.ChannelSelection
import kotlinx.android.synthetic.main.widget_dmchannel_item.view.*

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
            AppState.global.channelSelection.value =
                ChannelSelection(guildId = "@me", channelId = dmChannel.id)
            AppState.global.eventBus.postEvent(
                AppUIEvent(
                    AppUIEventType.CHANNEL_DRAWER,
                    false
                )
            )
        }
        itemView.dmchannel_user_avatar.user = dmChannel.recipient
        itemView.text_name.text = dmChannel.recipient?.discriminator
        itemView.dm_user_unread_tv.visibility = if (dmChannel.unReadCount>0 && dmChannel.unread) View.VISIBLE else View.GONE
        itemView.dm_user_unread_tv.text = dmChannel.unReadCount.toString()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bind(holder.itemView, dmChannelList[position])
    }
}