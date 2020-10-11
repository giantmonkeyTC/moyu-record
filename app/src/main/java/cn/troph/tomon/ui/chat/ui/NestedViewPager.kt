package cn.troph.tomon.ui.chat.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.SwitchEmojiPreviewEvent
import cn.troph.tomon.core.utils.event.observeEventOnUi
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.emoji_image.view.*

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

    fun observeEmojiPreview(){
        Client.global.eventBus.observeEventOnUi<SwitchEmojiPreviewEvent>().subscribe(
            Consumer { event ->
               requestDisallowInterceptTouchEvent(true)
            })
    }
}