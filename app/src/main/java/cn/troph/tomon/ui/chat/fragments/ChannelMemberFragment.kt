package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.*
import cn.troph.tomon.ui.chat.members.MemberListAdapter
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.fragment_channel_member.*

class ChannelMemberFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_channel_member, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                val mAdapter: MemberListAdapter<GuildMember> = MemberListAdapter(it)
                view_members.layoutManager = LinearLayoutManager(view.context)
                view_members.adapter = mAdapter
                if (view_members.itemDecorationCount > 0) {
                    view_members.removeItemDecorationAt(0)
                }
                val headersDecor = StickyRecyclerHeadersDecoration(mAdapter)
                view_members.addItemDecoration(headersDecor)
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
                val mAdapter: MemberListAdapter<User> = MemberListAdapter(it)
                view_members.layoutManager = LinearLayoutManager(view.context)
                view_members.adapter = mAdapter
                if (view_members.itemDecorationCount > 0) {
                    view_members.removeItemDecorationAt(0)
                }
                mAdapter.notifyDataSetChanged()
                view_members.addItemDecoration(StickyRecyclerHeadersDecoration(mAdapter))
            }
        })
    }

}