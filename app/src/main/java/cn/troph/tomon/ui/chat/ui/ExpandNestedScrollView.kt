package cn.troph.tomon.ui.chat.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.NestedScrollView

class ExpandNestedScrollView(context: Context, attrs: AttributeSet?):NestedScrollView(context,attrs) {

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(target, dx, dy, consumed)
    }
}