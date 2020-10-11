package cn.troph.tomon.ui.chat.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.SwitchEmojiPreviewEvent
import cn.troph.tomon.core.utils.event.observeEventOnUi
import io.reactivex.rxjava3.functions.Consumer

class NestedRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    fun observeEmojiPreview() {
        Client.global.eventBus.observeEventOnUi<SwitchEmojiPreviewEvent>().subscribe(
            Consumer { event ->
                requestDisallowInterceptTouchEvent(true)
            })
    }

}