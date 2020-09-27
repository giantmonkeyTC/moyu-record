package cn.troph.tomon.ui.chat.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.ViewPager
import cn.troph.tomon.R
import cn.troph.tomon.core.utils.DensityUtil
import cn.troph.tomon.ui.activities.ChannelInfoActivity
import cn.troph.tomon.ui.states.AppState
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_channel_detail.*


class ExpandNestedScrollView(context: Context, attrs: AttributeSet?) :
    NestedScrollView(context, attrs) {
    private lateinit var header: ConstraintLayout
    private lateinit var tab: TabLayout
    private lateinit var viewPager: ViewPager
    private var mHeaderHeight: Int = 0


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
            measuredHeight - tab.measuredHeight - DensityUtil.dip2px(
                context,
                ChannelInfoActivity.actionBarMoveY
            )
    }


    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (scrollY >= header.height - DensityUtil.dip2px(
                context,
                ChannelInfoActivity.actionBarMoveY
            )
        ) {
            //up scroll
            if (target.canScrollVertically(1) && dy > 0)
                consumed[1] = 0
            if (!target.canScrollVertically(-1) && dy < 0) {
                consumed[1] = dy
                scrollTo(0, scrollY + dy)
            }
        } else {
            if (scrollY + dy > header.height - DensityUtil.dip2px(
                    context,
                    ChannelInfoActivity.actionBarMoveY
                )
            )
                scrollTo(
                    0, header.height - DensityUtil.dip2px(
                        context,
                        ChannelInfoActivity.actionBarMoveY
                    )
                )
            else
                scrollTo(0, scrollY + dy)
            consumed[1] = dy
        }
    }


}