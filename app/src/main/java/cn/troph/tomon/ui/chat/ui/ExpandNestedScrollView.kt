package cn.troph.tomon.ui.chat.ui

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.Interpolator
import android.widget.OverScroller
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import cn.troph.tomon.R
import com.google.android.material.tabs.TabLayout
import com.orhanobut.logger.Logger


class ExpandNestedScrollView(context: Context, attrs: AttributeSet?) :
    NestedScrollView(context, attrs) {
    private lateinit var header: ConstraintLayout
    private lateinit var tab: TabLayout
    private lateinit var viewPager: ViewPager
    private var mHeaderHeight: Int = 0
    private var mScroller: OverScroller = OverScroller(context)
    private lateinit var mOffsetAnimator: ValueAnimator
    private lateinit var mInterpolator: Interpolator
    private var mTouchSlop = 0
    private var mMaximumVelocity = 0
    private var mMinimumVelocity: Int = 0

    private var mLastY = 0f
    private var mDragging = false

    init {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop()
        mMaximumVelocity = ViewConfiguration.get(context)
            .getScaledMaximumFlingVelocity()
        mMinimumVelocity = ViewConfiguration.get(context)
            .getScaledMinimumFlingVelocity()
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        Logger.d("start")
        return (ViewCompat.SCROLL_AXIS_VERTICAL and nestedScrollAxes) != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        Logger.d("accept")
    }

    override fun onStopNestedScroll(target: View) {
        Logger.d("stop")
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        Logger.d("scrolling")
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


    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        var consume = consumed
        if (target is RecyclerView && velocityY < 0) {
            val membersList = target as RecyclerView
            val firstChild = membersList.getChildAt(0)
            val childPosition = membersList.getChildAdapterPosition(firstChild)
            consume = childPosition > 3
        }
        if (!consume) {
            animateScroll(velocityY, computeDuration(0f), consumed)
        } else {
            animateScroll(velocityY, computeDuration(velocityY), consumed)
        }
        return true
    }

    override fun getNestedScrollAxes(): Int {
        return 0
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        var hiddenHeader = dy > 0 && scrollY < mHeaderHeight
        var showHeader = dy < 0 && scrollY > 0 && !canScrollVertically(-1)
        if (hiddenHeader || showHeader) {
            scrollBy(0, dy)
            consumed[1] = dy
        }
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    private fun animateScroll(velocityY: Float, duration: Int, consumed: Boolean) {
        val currentOffset = scrollY
        val topHeight: Int = header.getHeight()
        if (mOffsetAnimator == null) {
            mOffsetAnimator = ValueAnimator()
            mOffsetAnimator.interpolator = mInterpolator
            mOffsetAnimator.addUpdateListener { animation ->
                if (animation.animatedValue is Int) {
                    scrollTo(0, (animation.animatedValue as Int))
                }
            }
        } else {
            mOffsetAnimator.cancel()
        }
        mOffsetAnimator.duration = Math.min(duration, 600).toLong()
        if (velocityY >= 0) {
            mOffsetAnimator.setIntValues(currentOffset, topHeight)
            mOffsetAnimator.start()
        } else {
            //如果子View没有消耗down事件 那么就让自身滑倒0位置
            if (!consumed) {
                mOffsetAnimator.setIntValues(currentOffset, 0)
                mOffsetAnimator.start()
            }
        }
    }

    private fun computeDuration(velocityY: Float): Int {
        var velocityY = velocityY
        val distance: Int
        if (velocityY > 0) {
            distance = Math.abs(header.getHeight() - scrollY)
        } else {
            distance = Math.abs(header.getHeight() - (header.getHeight() - scrollY))
        }
        val duration: Int
        velocityY = Math.abs(velocityY)
        duration = if (velocityY > 0) {
            3 * Math.round(1000 * (distance / velocityY))
        } else {
            val distanceRatio = distance.toFloat() / height
            ((distanceRatio + 1) * 150).toInt()
        }
        return duration
    }

    override fun scrollTo(x: Int, y: Int) {
        var y = y
        if (y < 0) {
            y = 0
        }
        if (y > mHeaderHeight) {
            y = mHeaderHeight
        }
        if (y != scrollY) {
            super.scrollTo(x, y)
        }
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.currY)
            invalidate()
        }
    }


}