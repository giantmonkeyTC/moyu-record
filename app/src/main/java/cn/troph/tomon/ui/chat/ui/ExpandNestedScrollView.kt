package cn.troph.tomon.ui.chat.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.ViewPager
import cn.troph.tomon.R
import cn.troph.tomon.core.utils.DensityUtil
import cn.troph.tomon.ui.chat.fragments.ChannelInfoFragment
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
    private var onScrollListener: ExpandNestedScrollView.OnScrollListener? = null


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
        params.height =
            measuredHeight - tab.measuredHeight + ((1 - AppState.global.scrollPercent.value) * DensityUtil.dip2px(
                context,
                ChannelInfoFragment.actionBarMoveY
            )).toInt()
    }

//    override fun scrollTo(x: Int, y: Int) {
//        super.scrollTo(x, y)
//        if (y == 0) {
//            onScrollListener?.let {
//                it.onReset()
//            }
//        }
//    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (scrollY >= header.height) {
            consumed[1] = 0
        } else {
            scrollBy(0, dy)
            consumed[1] = dy
        }
    }



    fun setOnScrollListener(onScrollListener: ExpandNestedScrollView.OnScrollListener?) {
        this.onScrollListener = onScrollListener
    }


    fun resetViewpagerHeight() {
        val params = viewPager.layoutParams
        params.height =
            params.height + DensityUtil.dip2px(context, ChannelInfoFragment.actionBarMoveY)
    }

    interface OnScrollListener {
        fun onScroll() {
        }

        fun onReset() {

        }
    }


}