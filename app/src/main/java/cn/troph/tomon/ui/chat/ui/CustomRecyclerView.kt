package cn.troph.tomon.ui.chat.ui

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        e?.let {
            if (it.action == MotionEvent.ACTION_UP) {

            }
        }
        return super.onTouchEvent(e)
    }
}