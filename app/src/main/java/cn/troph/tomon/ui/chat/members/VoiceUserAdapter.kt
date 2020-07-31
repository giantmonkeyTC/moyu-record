package cn.troph.tomon.ui.chat.members

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.User
import kotlinx.android.synthetic.main.item_voice_avatar.view.*

class VoiceUserAdapter(private val userList: MutableList<User>) :
    RecyclerView.Adapter<VoiceUserHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceUserHolder {
        return VoiceUserHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_voice_avatar, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VoiceUserHolder, position: Int) {
        holder.itemView.voice_user_avatar.user = userList[position]

        if (userList[position].isSpeaking) {
            holder.itemView.speaking_circle_iv.borderColor =
                holder.itemView.context.getColor(R.color.speaking)
        } else {
            holder.itemView.speaking_circle_iv.borderColor =
                holder.itemView.context.getColor(R.color.white)
        }
        holder.itemView.user_name_voice.text = userList[position].name
        holder.itemView.voice_mic.isChecked = userList[position].isSelfMute
        holder.itemView.voice_deaf.isChecked = userList[position].isSelfDeaf
        if (userList[position].id == Client.global.me.id) {
            holder.itemView.voice_mic.visibility = View.GONE
            holder.itemView.voice_deaf.visibility = View.GONE
        } else {
            holder.itemView.voice_mic.visibility = View.VISIBLE
            holder.itemView.voice_deaf.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}

class VoiceUserHolder(view: View) : RecyclerView.ViewHolder(view)