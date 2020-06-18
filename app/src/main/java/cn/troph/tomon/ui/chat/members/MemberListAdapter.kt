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
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import kotlinx.android.synthetic.main.bottom_sheet_member_detail.view.*
import kotlinx.android.synthetic.main.widget_member_item.view.*
import kotlinx.android.synthetic.main.widget_member_roles.view.*
import kotlinx.android.synthetic.main.widget_role_list_header.view.*

class MemberListAdapter(private val memberList: MutableList<GuildMember>) :
    RecyclerView.Adapter<MemberListAdapter.ViewHolder>(),
    StickyRecyclerHeadersAdapter<MemberListAdapter.HeaderViewHolder> {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

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
        itemView.widget_member_name_text.setTextColor(
            (if (member.roles.color == null)
                0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt())
        )
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
                    (if (member.roles.color == null)
                        0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt()),
                    member.displayName
                )
            }
        view.widget_member_name_detail_text.text =
            TextUtils.concat(displaynameSpan, discriminatorSpan)
        rolesBinder(itemView = view, member = member)
        val dialog = BottomSheetDialog(parent.context)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun rolesBinder(itemView: View, member: GuildMember) {
        itemView.member_detail_roles.removeAllViews()
        member.roles.sequence.forEach explicit@{ role ->
            if (role.isEveryone)
                return@explicit
            val layoutInflater = LayoutInflater.from(itemView.context)
            val role_view = layoutInflater.inflate(R.layout.widget_member_roles, null)
            role_view.role_name.text = role.name
            itemView.member_detail_roles.addView(role_view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bind(holder.itemView, memberList[position])
    }

    override fun getHeaderId(position: Int): Long {
        return memberList[position].roles.highest!!.index.toLong()
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): HeaderViewHolder {
        val view = LayoutInflater.from(parent!!.context)
            .inflate(R.layout.widget_role_list_header, parent, false)
        return HeaderViewHolder(view)
    }

    private fun bindHeader(itemView: View, member: GuildMember) {
        itemView.widget_role_list_header_text.text = member.roles.highest!!.name
    }

    override fun onBindHeaderViewHolder(p0: HeaderViewHolder?, p1: Int) {

        if (p0 != null) {
            bindHeader(p0.itemView, memberList[p1])
        }
    }

}