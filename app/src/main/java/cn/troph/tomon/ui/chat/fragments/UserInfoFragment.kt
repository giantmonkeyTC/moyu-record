package cn.troph.tomon.ui.chat.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import cn.troph.tomon.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UserInfoFragment : BottomSheetDialogFragment() {

    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.fragment_user_information, null)
        bottomSheet.setContentView(view)
        val appBarLayout = view.findViewById<AppBarLayout>(R.id.appbar_layout)
        val profileLayout = view.findViewById<ConstraintLayout>(R.id.profile_layout)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        showView(appBarLayout, getActionBarSize())
                        hideAppBar(profileLayout)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        hideAppBar(appBarLayout)
                        showView(profileLayout, getActionBarSize())
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
}