package cn.troph.tomon.ui.chat.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NestedViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    private val globalRect: Rect = Rect() // 临时数据

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val x = ev?.rawX?.toInt()
        val y = ev?.rawY?.toInt()
        getGlobalVisibleRect(globalRect)
        if (globalRect.contains(x!!, y!!)) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return false
    }
}