package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.core.structures.User
import cn.troph.tomon.ui.chat.emoji.EmojiFragment
import cn.troph.tomon.ui.chat.emoji.OnEmojiClickListener
import cn.troph.tomon.ui.chat.members.MemberListAdapter
import cn.troph.tomon.ui.chat.ui.NestedViewPager
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.fragment_channel_detail.*
import kotlinx.android.synthetic.main.fragment_channel_member.*
import kotlinx.android.synthetic.main.fragment_channel_panel.*


class ChannelInfoFragment : Fragment() {
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager
    private val mChatSharedViewModel:ChatSharedViewModel by activityViewModels()
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
//        viewPagerAdapter =
//            ViewPagerAdapter(requireFragmentManager())
//        viewPager = view.findViewById(R.id.channel_info_viewpager)
//        viewPager.adapter = viewPagerAdapter
//        channel_info_description.setText("三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二三十二")
        chatSharedViewModel.channelSelectionLD.observe(viewLifecycleOwner, Observer {
            channelId = it.channelId
        })

        chatSharedViewModel.presenceUpdateLV.observe(viewLifecycleOwner, Observer {
            channelId?.let { it1 ->
                if (Client.global.channels[it1] !is DmChannel)
                    chatSharedViewModel.loadMemberList(it1)
                else if (Client.global.channels[it1] is DmChannel)
                    chatSharedViewModel.loadDmMemberList(it1)
            }
        })
       
        chatSharedViewModel.memberLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                val mAdapter: MemberListAdapter<GuildMember> = MemberListAdapter(it,requireContext())
                channel_info_member_list.layoutManager = LinearLayoutManager(view.context)
                channel_info_member_list.adapter = mAdapter
                if (channel_info_member_list.itemDecorationCount > 0) {
                    channel_info_member_list.removeItemDecorationAt(0)
                }
                val headersDecor = StickyRecyclerHeadersDecoration(mAdapter)
                channel_info_member_list.addItemDecoration(headersDecor)
                mAdapter.notifyDataSetChanged()
                mAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        headersDecor.invalidateHeaders()
                    }
                })
            }
        })
        chatSharedViewModel.dmMemberLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                val mAdapter: MemberListAdapter<User> = MemberListAdapter(it,requireContext())
                channel_info_member_list.layoutManager = LinearLayoutManager(view.context)
                channel_info_member_list.adapter = mAdapter
                if (channel_info_member_list.itemDecorationCount > 0) {
                    channel_info_member_list.removeItemDecorationAt(0)
                }
                mAdapter.notifyDataSetChanged()
                channel_info_member_list.addItemDecoration(StickyRecyclerHeadersDecoration(mAdapter))
            }
        })
   
   
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