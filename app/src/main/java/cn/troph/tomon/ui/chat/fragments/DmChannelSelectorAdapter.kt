package cn.troph.tomon.ui.chat.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.User
import kotlinx.android.synthetic.main.widget_dmchannel_item.view.*

class DmChannelSelectorAdapter(private val dmChannelList: MutableList<DmChannel>) : RecyclerView.Adapter<DmChannelSelectorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DmChannelSelectorAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.widget_dmchannel_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return dmChannelList.size
    }
    private fun bind(itemView: View,dmChannel: DmChannel){
        itemView.dmchannel_user_avatar.user = dmChannel.recipient
        itemView.text_name.text = dmChannel.recipient?.discriminator
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bind(holder.itemView, dmChannelList[position])
    }
}