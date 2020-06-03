package cn.troph.tomon.ui.chat.members

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableString
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.utils.color
import cn.troph.tomon.core.utils.spannable
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_sheet_member_detail.view.*
import kotlinx.android.synthetic.main.widget_member_item.view.*
import kotlinx.android.synthetic.main.widget_member_roles.view.*
import kotlinx.android.synthetic.main.widget_message_item.view.*

class MemberListAdapter(private val memberList: MutableList<GuildMember>) :
    RecyclerView.Adapter<MemberListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.widget_member_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    private fun bind(itemView: View, member: GuildMember) {
        itemView.setOnClickListener {
            callMemberDetail(parent = itemView as ViewGroup, member = member)

        }
        itemView.member_avatar.user = member.user
        itemView.widget_member_name_text.text = member.displayName
        itemView.widget_member_name_text.setTextColor((member.roles.color!!.color or 0xFF000000.toInt()))
    }

    private fun callMemberDetail(parent: ViewGroup, member: GuildMember) {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_member_detail, null)
        view.detail_member_avatar.user = member.user
        val discriminatorSpan: SpannableString =
            spannable { color(Color.GRAY, " #" + member.user!!.discriminator) }
        val displaynameSpan: SpannableString =
            spannable {
                color(
                    (member.roles.color!!.color or 0xFF000000.toInt()),
                    member.displayName
                )
            }
        member.roles
        view.widget_member_name_detail_text.text =
            TextUtils.concat(displaynameSpan, discriminatorSpan)
        rolesBinder(itemView = view, member = member)
        val dialog = BottomSheetDialog(parent.context)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun rolesBinder(itemView: View, member: GuildMember) {
        for (role in member.roles.sequence) {
            val layoutInflater = LayoutInflater.from(itemView.context)
            val role_view = layoutInflater.inflate(R.layout.widget_member_roles, null)
            role_view.role_name.text = role.name
            itemView.member_detail_roles.addView(role_view)
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = memberList[position]
        bind(holder.itemView, memberList[position])

    }
}