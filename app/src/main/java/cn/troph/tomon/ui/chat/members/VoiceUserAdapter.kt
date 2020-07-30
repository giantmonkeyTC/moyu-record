package cn.troph.tomon.ui.chat.members

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
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
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}

class VoiceUserHolder(view: View) : RecyclerView.ViewHolder(view)