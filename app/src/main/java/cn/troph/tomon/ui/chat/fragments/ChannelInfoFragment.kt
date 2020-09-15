package cn.troph.tomon.ui.chat.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import at.blogc.android.views.ExpandableTextView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.core.structures.User
import cn.troph.tomon.core.utils.DensityUtil
import cn.troph.tomon.ui.chat.emoji.EmojiFragment
import cn.troph.tomon.ui.chat.emoji.OnEmojiClickListener
import cn.troph.tomon.ui.chat.members.MemberListAdapter
import cn.troph.tomon.ui.chat.ui.NestedScrollViewPager
import cn.troph.tomon.ui.chat.ui.NestedViewPager
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.orhanobut.logger.Logger
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.fragment_channel_detail.*
import kotlinx.android.synthetic.main.fragment_channel_member.*
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import kotlin.math.exp


class ChannelInfoFragment : Fragment() {
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: NestedViewPager
    private lateinit var tabLayout: TabLayout
    private val chatSharedViewModel: ChatSharedViewModel by activityViewModels()

    private var channelId: String? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed && value != null) {
                val channel = Client.global.channels[value]
                if (channel is TextChannelBase && channel !is DmChannel) {
                    chatSharedViewModel.loadMemberList(value)
                } else if (channel is DmChannel) {
                    chatSharedViewModel.loadDmMemberList(value)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val channelInfoDescription =
            view.findViewById<ExpandableTextView>(R.id.channel_info_description)
        val expandIcon = view.findViewById<ImageView>(R.id.ic_expand_text)
        val channelInfo = view.findViewById<TextView>(R.id.channel_info)
        channel_info_name.setText("哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈")
        channelInfoDescription.setInterpolator(OvershootInterpolator())
        channelInfoDescription.setText("哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈")
        channelInfoDescription.post {
            val layout = channelInfoDescription.layout
            val lines = layout.lineCount
            if (lines > 0) {
                val count = layout.getEllipsisCount(lines - 1)
                if (count == 0) {
                    space_expand.visibility = View.VISIBLE
                    expandIcon.visibility = View.GONE
                } else {
                    space_expand.visibility = View.GONE
                    expandIcon.visibility = View.VISIBLE
                }

            }
        }
        AppState.global.scrollPercent.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                channelInfo.alpha = 1 - it
                if (it == 1f)
                    channel_info_name.visibility = View.VISIBLE
                else
                    channel_info_name.visibility = View.GONE
            }
        expandIcon.setOnClickListener {
            if (channelInfoDescription.isExpanded) {
                channelInfoDescription.collapse()
                expandIcon.setImageResource(R.drawable.channel_info_expand_arrow)
            } else {
                channelInfoDescription.expand()
                expandIcon.setImageResource(R.drawable.channel_info_collapse_arrow)
            }
        }
        viewPagerAdapter =
            ViewPagerAdapter(requireFragmentManager())
        viewPager = view.findViewById(R.id.channel_info_viewpager)
        viewPager.adapter = viewPagerAdapter
        tabLayout = view.findViewById(R.id.channel_info_tab)
//        tabLayout.addTab(tabLayout.newTab().apply {
//            this.
//            text = "成员"
//        })
//        tabLayout.addTab(tabLayout.newTab().apply {
//            text = "@我"
//        })
//        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//
//            }
//
//            override fun onPageSelected(position: Int) {
//                tabLayout.getTabAt(position)?.select()
//            }
//
//            override fun onPageScrollStateChanged(state: Int) {
//                when (state) {
//                    ViewPager.SCROLL_STATE_IDLE -> {
//                        Logger.d("idle")
//                    }
//                    ViewPager.SCROLL_STATE_DRAGGING -> {
//                        Logger.d("drag")
//                    }
//                    ViewPager.SCROLL_STATE_SETTLING -> {
//                        Logger.d("settle")
//                    }
//                }
//            }
//
//        })
        tabLayout.setupWithViewPager(viewPager)
        chatSharedViewModel.channelSelectionLD.observe(viewLifecycleOwner, Observer {
            channelId = it.channelId
        })

    }
}

class ViewPagerAdapter(
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager) {
    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "成员"
            1 -> "@我"
            else -> ""
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val channelMemberFragment = ChannelMemberFragment()
                return channelMemberFragment
            }
            1 -> {
                val mentionMefragment = MentionMeFragment()
                return mentionMefragment
            }
            else -> Fragment()
        }

    }

    override fun getCount(): Int {
        return 2
    }

}