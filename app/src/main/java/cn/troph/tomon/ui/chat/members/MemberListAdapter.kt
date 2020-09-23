package cn.troph.tomon.ui.chat.members

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.toColor
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Presence
import cn.troph.tomon.core.structures.User
import cn.troph.tomon.core.utils.DensityUtil
import cn.troph.tomon.core.utils.color
import cn.troph.tomon.core.utils.spannable
import cn.troph.tomon.ui.activities.ChatActivity
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
import kotlinx.android.synthetic.main.guild_user_info.view.*
import kotlinx.android.synthetic.main.widget_member_item.view.*
import kotlinx.android.synthetic.main.widget_member_roles.view.*
import kotlinx.android.synthetic.main.widget_role_list_header.view.*

class MemberListAdapter<T>(
    private val memberList: MutableList<T>,
    private val context: Context
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
                callMemberDetail(parent = itemView as ViewGroup, member = member)
            }
            itemView.member_avatar.user = member.user
            itemView.widget_member_description_text.visibility = View.GONE
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
        view.user_info_name.text =
            member.displayName
        view.user_info_discriminator.text =
            TextUtils.concat(member.user?.username, " #" + member.user!!.discriminator)

        rolesBinder(itemView = view, member = member)

        val dialog = BottomSheetDialog(parent.context)
        dialog.setContentView(view)
        dialog.window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            ?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        if (member.id != Client.global.me.id && member.id != "1") {
            view.user_info_menu.visibility = View.VISIBLE
            view.goto_dm.visibility = View.VISIBLE
            view.goto_dm.setOnClickListener {
                member.directMessage(member.id).observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        dialog.dismiss()
                        val intent = Intent(view.context, ChatActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("guildId", "@me")
                        bundle.putString("channelId", it["id"].asString)
                        intent.putExtras(bundle)
                        startActivity(
                            view.context,
                            intent,
                            ActivityOptions.makeCustomAnimation(
                                view.context,
                                R.anim.slide_in_right_custom,
                                R.anim.no_animation
                            ).toBundle()
                        )
                        AppState.global.channelSelection.value =
                            ChannelSelection(guildId = "@me", channelId = it["id"].asString)
                    }
            }
            view.user_info_menu.setOnClickListener {

                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val menu = inflater.inflate(R.layout.user_info_more_menu, null)
                val popUp = PopupWindow(
                    menu,
                    DensityUtil.dip2px(context, 108f),
                    DensityUtil.dip2px(context, 44f),
                    true
                )
                menu.setOnClickListener {
                    popUp.dismiss()
                    dialog.dismiss()
                    val reportF =
                        ReportFragment(
                            member.id,
                            1
                        ).apply {
                            show((view.context as AppCompatActivity).supportFragmentManager, null)
                        }

                }
                popUp.elevation = 10f

                popUp.showAsDropDown(
                    it,
                    DensityUtil.dip2px(context, 90f).unaryMinus(),
                    DensityUtil.dip2px(context, 10f)
                )
//                dialog.dismiss()
            }
        } else {
            view.user_info_menu.visibility = View.GONE
            view.goto_dm.visibility = View.GONE
        }

//        val extraSpace = view.findViewById<View>(R.id.extraSpace)
//        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
//        val peekHeightPx =
//            view.context.resources.getDimensionPixelSize(R.dimen.member_profile_peek_height)
//        bottomSheetBehavior.setPeekHeight(peekHeightPx)
//        extraSpace.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 2
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
                val drawable = ContextCompat.getDrawable(context, R.drawable.shape_role_item)
                drawable!!.colorFilter = PorterDuffColorFilter(
                    (if (role.color == 0)
                        0 or 0X1AFFFFFF else role.color or 0x1A000000), PorterDuff.Mode.OVERLAY
                )
                val role_view = layoutInflater.inflate(R.layout.widget_member_roles, null)
                role_view.widget_role_unit.background = drawable
                role_view.role_color.imageTintList = ColorStateList.valueOf(
                    (if (role.color == 0)
                        0 or 0XFFFFFFFF.toInt() else role.color or 0xFF000000.toInt())
                )
                role_view.role_name.text = role.name
                role_view.role_name.setTextColor(
                    (if (role.color == 0)
                        0 or 0XFFFFFFFF.toInt() else role.color or 0xFF000000.toInt())
                )
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