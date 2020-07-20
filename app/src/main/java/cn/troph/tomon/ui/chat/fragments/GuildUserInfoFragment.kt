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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.GuildMemberService
import cn.troph.tomon.ui.chat.viewmodel.UserInfoViewModel
import cn.troph.tomon.ui.widgets.UserAvatar
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.guild_user_info.*

class GuildUserInfoFragment(private val userId: String) : BottomSheetDialogFragment() {
    private val mUserVM: UserInfoViewModel by viewModels()
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
        mUserVM.loadGuildUserInfo(userId)
        val avatar = view.findViewById<UserAvatar>(R.id.user_info_avatar)
        val name = view.findViewById<TextView>(R.id.user_info_name)
        val nick = view.findViewById<TextView>(R.id.user_info_nick)
        val out = view.findViewById<TextView>(R.id.user_sign_out)
        val roles = view.findViewById<ConstraintLayout>(R.id.role_section)
        roles.visibility = View.GONE
        mUserVM.guildUserInfoLD.observe(viewLifecycleOwner, Observer { user ->
            avatar.user = user
            name.text = user.name
            nick.text = "${user.username} #${user.discriminator}"
            out.setOnClickListener {
                dismiss()
                ReportFragment(
                    userId,
                    1
                ).show((view.context as AppCompatActivity).supportFragmentManager, null)
            }

            user_private_chat.setOnClickListener {
                Toast.makeText(requireContext(), R.string.no_pchat_support, Toast.LENGTH_SHORT)
                    .show()
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