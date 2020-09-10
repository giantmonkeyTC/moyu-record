package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.viewpager.widget.ViewPager
import cn.troph.tomon.R
import cn.troph.tomon.ui.chat.emoji.EmojiFragment
import cn.troph.tomon.ui.chat.emoji.OnEmojiClickListener
import cn.troph.tomon.ui.chat.ui.NestedViewPager
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import kotlinx.android.synthetic.main.fragment_channel_detail.*
import kotlinx.android.synthetic.main.fragment_channel_panel.*


class ChannelInfoFragment : Fragment() {
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager
    private val mChatSharedViewModel:ChatSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerAdapter =
            ViewPagerAdapter(requireFragmentManager())
        viewPager = view.findViewById(R.id.channel_info_viewpager)
        viewPager.adapter = viewPagerAdapter
//        channel_info_description.setText("三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二")
    }
}
class ViewPagerAdapter(
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = ChannelMemberFragment()
                return fragment
            }
            else -> Fragment()
        }

    }

    override fun getCount(): Int {
        return 1
    }

}