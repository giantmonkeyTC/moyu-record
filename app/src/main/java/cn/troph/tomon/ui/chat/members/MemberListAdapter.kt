package cn.troph.tomon.ui.chat.members

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableString
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColor
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Presence
import cn.troph.tomon.core.structures.User
import cn.troph.tomon.core.utils.color
import cn.troph.tomon.core.utils.spannable
import cn.troph.tomon.ui.chat.fragments.DmChannelSelectorFragment
import cn.troph.tomon.ui.chat.fragments.ReportFragment
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.AppUIEvent
import cn.troph.tomon.ui.states.AppUIEventType
import cn.troph.tomon.ui.states.ChannelSelection
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.bottom_sheet_member_detail.view.*
import kotlinx.android.synthetic.main.bottom_sheet_member_detail.view.member_detail_roles
import kotlinx.android.synthetic.main.fragment_guild_selector.*
import kotlinx.android.synthetic.main.guild_user_info.view.*
import kotlinx.android.synthetic.main.widget_member_item.view.*
import kotlinx.android.synthetic.main.widget_member_roles.view.*
import kotlinx.android.synthetic.main.widget_role_list_header.view.*

class MemberListAdapter<T>(
    private val memberList: MutableList<T>
) :
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

    private fun bind(itemView: View, member: T) {
        if (member is GuildMember) {
            itemView.setOnClickListener {
                if (member.id != Client.global.me.id)
                    callMemberDetail(parent = itemView as ViewGroup, member = member)
            }
            itemView.member_avatar.user = member.user
            itemView.widget_member_name_text.text = member.displayName
            if (Client.global.presences[member.id]?.status == "offline") {
                itemView.guild_user_online.visibility = View.GONE
                itemView.widget_member_name_text.setTextColor(
                    (if (member.roles.color == null)
                        0 or 0X60FFFFFF.toInt() else member.roles.color!!.color or 0x60000000.toInt())
                )
                itemView.offline_user_shadow.visibility = View.VISIBLE
            } else {
                itemView.guild_user_online.visibility = View.VISIBLE
                itemView.offline_user_shadow.visibility = View.GONE
                itemView.widget_member_name_text.setTextColor(
                    (if (member.roles.color == null)
                        0 or 0XFFFFFFFF.toInt() else member.roles.color!!.color or 0xFF000000.toInt())
                )
            }


        } else if (member is User) {
            itemView.member_avatar.user = member
            itemView.widget_member_name_text.text = member.name
            if (Client.global.presences[member.id]?.status == "offline") {
                itemView.guild_user_online.visibility = View.GONE
                itemView.widget_member_name_text.setTextColor(0x60FFFFFF)
                itemView.offline_user_shadow.visibility = View.VISIBLE
            } else {
                itemView.guild_user_online.visibility = View.VISIBLE
                itemView.widget_member_name_text.setTextColor(0xFFFFFFFF.toInt())
                itemView.offline_user_shadow.visibility = View.GONE
            }
        }

    }

    private fun callMemberDetail(parent: ViewGroup, member: GuildMember) {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.guild_user_info, null)
        view.user_info_avatar.user = member.user
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
        view.user_info_name.text =
            displaynameSpan
        view.user_info_discriminator.text = discriminatorSpan
        view.user_info_nick.text = TextUtils.concat(member.displayName, discriminatorSpan)

        rolesBinder(itemView = view, member = member)

        val dialog = BottomSheetDialog(parent.context)
        dialog.setContentView(view)
        dialog.window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            ?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        view.user_private_chat.setOnClickListener {
            member.directMessage(member.id).observeOn(AndroidSchedulers.mainThread()).subscribe {
                dialog.dismiss()
                AppState.global.channelSelection.value =
                    ChannelSelection(guildId = "@me", channelId = it["id"].asString)
                AppState.global.eventBus.postEvent(
                    AppUIEvent(
                        AppUIEventType.MEMBER_DRAWER,
                        false
                    )
                )
            }
        }
        view.user_sign_out.setOnClickListener {
            dialog.dismiss()
            ReportFragment(
                member.id,
                1
            ).show((view.context as AppCompatActivity).supportFragmentManager, null)
        }
        val extraSpace = view.findViewById<View>(R.id.extraSpace)
        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        val peekHeightPx =
            view.context.resources.getDimensionPixelSize(R.dimen.member_profile_peek_height)
        bottomSheetBehavior.setPeekHeight(peekHeightPx)
        extraSpace.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 2
        dialog.show()
    }

    private fun rolesBinder(itemView: View, member: GuildMember) {
        itemView.member_detail_roles.removeAllViews()
        if (member.roles.sequence.size == 1) {
            itemView.role_section.visibility = View.GONE
        } else
            member.roles.sequence.forEach explicit@{ role ->
                if (role.isEveryone)
                    return@explicit
                val layoutInflater = LayoutInflater.from(itemView.context)
                val role_view = layoutInflater.inflate(R.layout.widget_member_roles, null)
                role_view.role_color.background = (
                        ColorDrawable(
                            (if (role.color == 0)
                                0 or 0XFFFFFFFF.toInt() else role.color or 0xFF000000.toInt())
                        )
                        )
                role_view.role_name.text = role.name
                itemView.member_detail_roles.addView(role_view)
            }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bind(holder.itemView, memberList[position])
    }

    override fun getHeaderId(position: Int): Long {
        return if (memberList[position] is GuildMember) {
            if (Client.global.presences[(memberList[position] as GuildMember).id]?.status == "offline")
                0xffffff
            else
                (memberList[position] as GuildMember).roles.highest!!.index.toLong()
        } else
            -1
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): HeaderViewHolder {
        val view = LayoutInflater.from(parent!!.context)
            .inflate(R.layout.widget_role_list_header, parent, false)
        return HeaderViewHolder(view)
    }

    private fun bindHeader(itemView: View, member: T) {
        if (member is GuildMember) {
            if (Client.global.presences[member.id]?.status == "offline")
                itemView.widget_role_list_header_text.text = "离线"
            else
                itemView.widget_role_list_header_text.text = member.roles.highest!!.name
        } else if (member is User)
            itemView.widget_role_list_header.visibility = View.GONE
    }

    override fun onBindHeaderViewHolder(p0: HeaderViewHolder?, p1: Int) {
        if (p0 != null) {
            bindHeader(p0.itemView, memberList[p1])
        }
    }
}