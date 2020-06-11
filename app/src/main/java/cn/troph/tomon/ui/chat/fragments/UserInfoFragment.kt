package cn.troph.tomon.ui.chat.fragments

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.activities.LoginActivity
import cn.troph.tomon.ui.chat.viewmodel.UserInfoViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_user_information.view.*

class UserInfoFragment : BottomSheetDialogFragment() {

    private val mUserInfoVM: UserInfoViewModel by viewModels()
    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mUserInfoVM.loadUserInfo()
        val bottomSheet = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_user_information, null)
        bottomSheet.setContentView(view)
        val appBarLayout = view.findViewById<AppBarLayout>(R.id.appbar_layout)
        val profileLayout = view.findViewById<ConstraintLayout>(R.id.profile_layout)
        val extraSpace = view.findViewById<View>(R.id.extraSpace)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO)
        extraSpace.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 2

        val me = mUserInfoVM.getUserInfoLiveData().value
        if (me != null) {
            view.profile_layout.user_info_avatar.user = me
            view.profile_layout.user_info_name.text = me.name
            view.profile_layout.user_info_email.text = me.email
            view.profile_layout.user_info_phone.text = me.phone
            view.profile_layout.user_info_nick.text = me.username
        }

        view.profile_layout.user_sign_out.setOnClickListener {
            Client.global.me.logout()
            gotoLogin()
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
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        dismiss()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
        hideAppBar(appBarLayout);
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

    private fun showView(view: View, size: Int) {
        val params = view.layoutParams
        params.height = size
        view.layoutParams = params
    }

    private fun getActionBarSize(): Int {
        val array =
            requireContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val size = array.getDimension(0, 0F).toInt()
        return size
    }

    private fun gotoLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(
            intent,
            ActivityOptions.makeCustomAnimation(
                requireContext(),
                android.R.anim.fade_in,
                android.R.anim.fade_out
            ).toBundle()
        )
    }
}