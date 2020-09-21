package cn.troph.tomon.ui.chat.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.activities.ChatActivity
import cn.troph.tomon.ui.activities.MeProfileActivity
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import com.google.android.gms.common.api.Api
import kotlinx.android.synthetic.main.fragment_me.*
import kotlinx.android.synthetic.main.fragment_user_information.view.*
import kotlinx.android.synthetic.main.widget_member_roles.view.*

class MeFragment : Fragment() {
    private val mChatVM: ChatSharedViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mChatVM.loadUserInfo()
        val me = mChatVM.userInfoLiveData.value
        if (me != null) {
            me_avatar.user = me
            me_name.text = me.username
            val drawable = ContextCompat.getDrawable(view.context, R.drawable.user_status)
            drawable!!.colorFilter = PorterDuffColorFilter(
                (if (Client.global.presences[me.id]?.status == "offline")
                    requireContext().getColor(R.color.switchOff) else requireContext().getColor(R.color.success)),
                PorterDuff.Mode.OVERLAY
            )
            if (Client.global.presences[me.id]?.status == "offline") {
                status_display.text = getString(R.string.offline)
                status_display.setTextColor(requireContext().getColor(R.color.switchOff))
            } else {
                status_display.text = getString(R.string.online)
                status_display.setTextColor(requireContext().getColor(R.color.success))
            }

            status_icon.background = drawable
            me_bio.visibility = View.GONE
            me_identifier.text = "#${me.discriminator}"
            logout.setOnClickListener {
                val logoutDialog = LogoutDialogFragment()
                logoutDialog.show(requireActivity().supportFragmentManager, "DIALOG_LOGOUT")
            }
            info_section.setOnClickListener {
                val intent = Intent(requireContext(), MeProfileActivity::class.java)
                startActivity(
                    intent,
                    ActivityOptions.makeCustomAnimation(
                        requireContext(),
                        R.anim.slide_in_right_custom,
                        R.anim.no_animation
                    ).toBundle()
                )
            }
        }
        mChatVM.userInfoLiveData.observe(viewLifecycleOwner, Observer {
            it?.let { me ->
                me_avatar.user = me
                me_name.text = me.username
                me_identifier.text = "#${me.discriminator}"
            }
        })

    }
}