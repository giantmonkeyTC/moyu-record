package cn.troph.tomon.ui.chat.fragments

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_user_information.view.*

class MentionFragment(val channelId:String) : BottomSheetDialogFragment(){

    private val mChatVM: ChatSharedViewModel by activityViewModels()
//    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mChatVM.loadMemberList(channelId)
        val bottomSheet = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.botom_sheet_mention, null)
        bottomSheet.setContentView(view)
        bottomSheet.window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            ?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        val multi = view.findViewById<AppBarLayout>(R.id.multi)
        val mentionLayout = view.findViewById<ConstraintLayout>(R.id.mention_panel)
//        val extraSpace = view.findViewById<View>(R.id.extraSpace)
//        val indicator = view.findViewById<View>(R.id.indicator)
//        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
//        val peekHeightPx = requireContext().resources
//            .getDimensionPixelSize(R.dimen.profile_peek_height)
//        bottomSheetBehavior.setPeekHeight(peekHeightPx)
//        extraSpace.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 2

        val me = mChatVM.userInfoLiveData.value
        if (me != null) {
        }

        view.profile_layout.user_sign_out.setOnClickListener {
            val logoutDialog = LogoutDialogFragment()
            logoutDialog.show(requireActivity().supportFragmentManager, "DIALOG_LOGOUT")
        }


//        bottomSheetBehavior.addBottomSheetCallback(object :
//            BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_EXPANDED -> {
//                        showView(appBarLayout, getActionBarSize())
//                    }
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
//                        hideAppBar(appBarLayout)
////                        showIndicator(indicator, indicator.measuredWidth, indicator.measuredHeight)
//                    }
//                    BottomSheetBehavior.STATE_HIDDEN -> {
//                        dismiss()
//                    }
//                }
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                if (slideOffset > 0) {
//                    setAppBarHeight(appBarLayout, slideOffset, getActionBarSize())
////                    setIndicatorShape(
////                        indicator,
////                        slideOffset,
////                        indicator.measuredWidth,
////                        indicator.measuredHeight
////                    )
//                }
//
//            }
//        })
        return bottomSheet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mChatVM.userInfoLiveData.observe(viewLifecycleOwner, Observer {
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
}