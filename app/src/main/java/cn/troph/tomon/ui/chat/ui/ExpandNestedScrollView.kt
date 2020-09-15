package cn.troph.tomon.ui.chat.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.ViewPager
import cn.troph.tomon.R
import cn.troph.tomon.ui.states.AppState
import com.google.android.material.tabs.TabLayout


class ExpandNestedScrollView(context: Context, attrs: AttributeSet?) :
    NestedScrollView(context, attrs) {
    private lateinit var header: ConstraintLayout
    private lateinit var tab: TabLayout
    private lateinit var viewPager: ViewPager
    private var mHeaderHeight: Int = 0
    private var mTouchSlop = 0
    private var mMaximumVelocity = 0
    private var mMinimumVelocity: Int = 0


    init {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop()
        mMaximumVelocity = ViewConfiguration.get(context)
            .getScaledMaximumFlingVelocity()
        mMinimumVelocity = ViewConfiguration.get(context)
            .getScaledMinimumFlingVelocity()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        header = findViewById(R.id.channel_info_header)
        tab = findViewById(R.id.channel_info_tab)
        viewPager = findViewById(R.id.channel_info_viewpager)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeaderHeight = header.measuredHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val params = viewPager.layoutParams
        params.height = measuredHeight - tab.measuredHeight
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (scrollY >= header.height){
            AppState.global.scrollPercent.value = 1f
            consumed[1] = 0
        }
        else {
            AppState.global.scrollPercent.value = scrollY.toFloat() / header.height.toFloat()
            scrollBy(0, dy)
            consumed[1] = dy
        }
    }


}