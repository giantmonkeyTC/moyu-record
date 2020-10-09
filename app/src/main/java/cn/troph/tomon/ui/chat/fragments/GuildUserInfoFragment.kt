package cn.troph.tomon.ui.chat.fragments

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels

import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.utils.DensityUtil
import cn.troph.tomon.ui.activities.ChatActivity
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.AppUIEvent
import cn.troph.tomon.ui.states.AppUIEventType
import cn.troph.tomon.ui.states.ChannelSelection

import cn.troph.tomon.ui.widgets.UserAvatar

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_guild_selector.*


import kotlinx.android.synthetic.main.guild_user_info.*
import kotlinx.android.synthetic.main.guild_user_info.view.*
import kotlinx.android.synthetic.main.widget_member_roles.view.*

class GuildUserInfoFragment(private val userId: String, private val member: GuildMember? = null) :
    BottomSheetDialogFragment() {
    private val mChatVM: ChatSharedViewModel by activityViewModels()
    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.Theme_App_Dialog_Bottomsheet)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (Client.global.users[userId]?.isDeletedUser() == true) {
            return
        }
        super.show(manager, tag)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return LayoutInflater.from(requireContext())
            .inflate(R.layout.guild_user_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mChatVM.loadGuildUserInfo(userId)
        val avatar = view.findViewById<UserAvatar>(R.id.user_info_avatar)
        val roles = view.findViewById<ConstraintLayout>(R.id.role_section)
        mChatVM.guildUserInfoLD.observe(viewLifecycleOwner, Observer { user ->
            avatar.user = user
            val channel =
                Client.global.dmChannels[mChatVM.channelSelectionLD.value?.channelId ?: ""]
            if (member == null) {
                roles.visibility = View.GONE
                view.user_info_name.text =
                    user.name
                view.user_info_discriminator.text =
                    TextUtils.concat(user.username, " #" + user.discriminator)
            } else {
                roles.visibility = View.VISIBLE
                rolesBinder(itemView = view, member = member)
                view.user_info_name.text =
                    member.displayName
                view.user_info_discriminator.text =
                    TextUtils.concat(member.user?.username, " #" + member.user!!.discriminator)

            }
            if (user.id != Client.global.me.id && user.id != "1") {
                view.user_info_menu.visibility = View.VISIBLE
                view.goto_dm.visibility = View.VISIBLE
                if (channel?.recipientId == user.id)
                    view.goto_dm.visibility = View.GONE
                goto_dm.setOnClickListener {
                    user.directMessage(user.id).observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            dialog?.dismiss()
                            val intent = Intent(requireContext(), ChatActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("guildId", "@me")
                            bundle.putString("channelId", it["id"].asString)
                            intent.putExtras(bundle)
                            startActivity(
                                intent,
                                ActivityOptions.makeCustomAnimation(
                                    requireContext(),
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
                        requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val menu = inflater.inflate(R.layout.user_info_more_menu, null)
                    val popUp = PopupWindow(
                        menu,
                        DensityUtil.dip2px(context, 108f),
                        DensityUtil.dip2px(context, 44f),
                        true
                    )
                    menu.setOnClickListener {
                        dismiss()
                        popUp.dismiss()
                        ReportFragment(
                            user.id,
                            1
                        ).show((view.context as AppCompatActivity).supportFragmentManager, null)
                    }

                    popUp.elevation = 10f
                    if (channel?.recipientId == user.id)
                        popUp.showAsDropDown(
                            it,
                            DensityUtil.dip2px(context, 110f).unaryMinus(),
                            DensityUtil.dip2px(context, 36f).unaryMinus()
                        )
                    else
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

        })

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
                val drawable =
                    ContextCompat.getDrawable(itemView.context, R.drawable.shape_role_item)
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

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val bottomSheet = super.onCreateDialog(savedInstanceState)
//        val view = View.inflate(context, R.layout.guild_user_info, null)
//        bottomSheet.setContentView(view)
//        bottomSheet.window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
//            ?.setBackgroundDrawable(
//                ColorDrawable(
//                    Color.TRANSPARENT
//                )
//            )
////        val extraSpace = view.findViewById<View>(R.id.extraSpace)
////        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
////        val peekHeightPx = requireContext().resources
////            .getDimensionPixelSize(R.dimen.profile_peek_height)
////        bottomSheetBehavior.setPeekHeight(peekHeightPx)
////        extraSpace.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 2
//
//        return bottomSheet
//    }

}