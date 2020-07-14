package cn.troph.tomon.ui.chat.fragments

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import anet.channel.util.Utils
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.activities.EntryOptionActivity
import cn.troph.tomon.ui.chat.viewmodel.UserInfoViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_user_information.view.*
import kotlin.math.log


class UserInfoFragment : BottomSheetDialogFragment() {

    private val mUserInfoVM: UserInfoViewModel by viewModels()
    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mUserInfoVM.loadUserInfo()
        val bottomSheet = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_user_information, null)
        bottomSheet.setContentView(view)
        bottomSheet.window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            ?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        val appBarLayout = view.findViewById<AppBarLayout>(R.id.appbar_layout)
        val profileLayout = view.findViewById<ConstraintLayout>(R.id.profile_layout)
        val extraSpace = view.findViewById<View>(R.id.extraSpace)
//        val indicator = view.findViewById<View>(R.id.indicator)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        val peekHeightPx = requireContext().resources
            .getDimensionPixelSize(R.dimen.profile_peek_height)
        bottomSheetBehavior.setPeekHeight(peekHeightPx)
        extraSpace.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 2

        val me = mUserInfoVM.getUserInfoLiveData().value
        if (me != null) {
            view.user_info_discriminator.text = "#${me.discriminator}"
            view.profile_layout.user_info_avatar.user = me
            view.profile_layout.user_info_name.text = me.name
            view.profile_layout.user_info_email.text =
                if (me.email == null) getString(R.string.profile_empty) else me.email
            view.profile_layout.user_info_phone.text =
                if (me.phone == null) getString(R.string.profile_empty) else me.phone
            view.profile_layout.user_info_nick.text = "${me.username} #${me.discriminator}"
        }

        view.profile_layout.user_sign_out.setOnClickListener {
        val logoutDialog = LogoutDialogFragment()
            logoutDialog.show(requireActivity().supportFragmentManager,"DIALOG_LOGOUT")
        }


        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        showView(appBarLayout, getActionBarSize())
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        hideAppBar(appBarLayout)
//                        showIndicator(indicator, indicator.measuredWidth, indicator.measuredHeight)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        dismiss()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset > 0) {
                    setAppBarHeight(appBarLayout, slideOffset, getActionBarSize())
//                    setIndicatorShape(
//                        indicator,
//                        slideOffset,
//                        indicator.measuredWidth,
//                        indicator.measuredHeight
//                    )
                }

            }
        })
        hideAppBar(appBarLayout)
        return bottomSheet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mUserInfoVM.getUserInfoLiveData().observe(viewLifecycleOwner, Observer {
            it?.let { me ->
                view.profile_layout.user_info_avatar.user = me
                view.profile_layout.user_info_name.text = me.name
                view.profile_layout.user_info_email.text = me.email
                view.profile_layout.user_info_phone.text = me.phone
                view.profile_layout.user_info_nick.text = me.username
            }
        })
    }

    override fun onStart() {
        super.onStart()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideAppBar(view: View) {
        val params = view.layoutParams
        params.height = 0
        view.layoutParams = params
    }

    private fun setAppBarHeight(view: View, height: Float, size: Int) {
        val params = view.layoutParams
        params.height = (size * height).toInt()
        view.alpha = height
        view.layoutParams = params
    }

//    private fun setIndicatorShape(view: View, increasingFactor: Float, width: Int, height: Int) {
//        val params = view.layoutParams
//        val factor = ((1 - increasingFactor) * 0.7).toFloat()
//        params.height = (height * factor).toInt()
//        params.width = (width * factor).toInt()
//        view.alpha = factor
//        view.layoutParams = params
//    }

    private fun showView(view: View, size: Int) {
        val params = view.layoutParams
        params.height = size
        view.layoutParams = params
    }

//    private fun showIndicator(view: View, width: Int, height: Int) {
//        val params = view.layoutParams
//        params.width = width
//        params.height = height
//        view.alpha = 1F
//        view.layoutParams = params
//    }

    private fun getActionBarSize(): Int {
        val array =
            requireContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val size = array.getDimension(0, 0F).toInt()
        return size
    }

    private fun gotoEntryOption() {
        val intent = Intent(requireContext(), EntryOptionActivity::class.java)
        startActivity(
            intent,
            ActivityOptions.makeCustomAnimation(
                requireContext(),
                R.animator.top_to_bottom_anim,
                R.animator.top_to_bottom_anim
            ).toBundle()
        )
        requireActivity().finish()
    }
}