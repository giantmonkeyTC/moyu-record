package cn.troph.tomon.ui.chat.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class NestedRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

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
}