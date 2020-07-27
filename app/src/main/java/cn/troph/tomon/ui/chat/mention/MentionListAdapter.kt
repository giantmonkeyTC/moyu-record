package cn.troph.tomon.ui.chat.mention

import android.graphics.Color
import android.text.SpannableString
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.utils.color
import cn.troph.tomon.core.utils.spannable
import kotlinx.android.synthetic.main.user_mention_item.view.*
import kotlinx.android.synthetic.main.widget_member_item.view.*

class MentionListAdapter(
    val mentionList: MutableList<GuildMember>,
    private val mentionSelectedListener: OnMentionSelectedListener
) : RecyclerView.Adapter<MentionListAdapter.MentionViewHolder>() {
    class MentionViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder {
        return MentionViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.user_mention_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mentionList.size
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        val member = mentionList[position]
        holder.itemView.setOnClickListener {
            mentionSelectedListener.onMentionSelected(member.id, member.displayName)
        }
        holder.itemView.mention_user_name.text = member.displayName
        val discriminatorSpan: SpannableString =
            spannable { color(Color.GRAY, " #" + member.user!!.discriminator) }
        val displaynameSpan: SpannableString =
            spannable {
                color(
                    (if (member.roles.color == null)
                        0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt()),
                    member.displayName
                )
            }
        if (Client.global.presences[member.id]?.status == "offline") {
            holder.itemView.mention_user_online.visibility = View.GONE
        } else {
            holder.itemView.mention_user_online.visibility = View.VISIBLE
        }
        holder.itemView.mention_user_discriminator.text =
            TextUtils.concat(displaynameSpan, discriminatorSpan)
        holder.itemView.mention_user_avatar.user = member.user
    }

    interface OnMentionSelectedListener {
        fun onMentionSelected(userId: String, userName: String)
    }
}