package cn.troph.tomon.ui.chat.fragments

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.GuildMember
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

class GuildUserInfoFragment(private val userId: String) : BottomSheetDialogFragment() {
    private val mChatVM: ChatSharedViewModel by activityViewModels()
    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
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
        val name = view.findViewById<TextView>(R.id.user_info_name)
        val nick = view.findViewById<TextView>(R.id.user_info_nick)
        val out = view.findViewById<TextView>(R.id.user_sign_out)
        val roles = view.findViewById<ConstraintLayout>(R.id.role_section)
        roles.visibility = View.GONE
        mChatVM.guildUserInfoLD.observe(viewLifecycleOwner, Observer { user ->
            avatar.user = user
            name.text = user.name
            nick.text = "${user.username} #${user.discriminator}"
            if (user.id != Client.global.me.id && user.id != "1") {
                out.visibility = View.VISIBLE
                user_private_chat.visibility = View.VISIBLE
                out.setOnClickListener {
                    dismiss()
                    ReportFragment(
                        userId,
                        1
                    ).show((view.context as AppCompatActivity).supportFragmentManager, null)
                }

                user_private_chat.setOnClickListener {
                    user.directMessage(user.id).observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            dialog?.dismiss()
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
            } else {
                out.visibility = View.GONE
                user_private_chat.visibility = View.GONE
            }

        })

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.guild_user_info, null)
        bottomSheet.setContentView(view)
        bottomSheet.window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            ?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        val extraSpace = view.findViewById<View>(R.id.extraSpace)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        val peekHeightPx = requireContext().resources
            .getDimensionPixelSize(R.dimen.profile_peek_height)
        bottomSheetBehavior.setPeekHeight(peekHeightPx)
        extraSpace.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 2

        return bottomSheet
    }

    override fun onStart() {
        super.onStart()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}