package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.*
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import io.reactivex.rxjava3.functions.Consumer

import kotlinx.android.synthetic.main.fragment_dmchannel_selector.*


class DmChannelSelectorFragment : Fragment() {
    private val mChatVM: ChatSharedViewModel by activityViewModels()
    private val mDMchannelList = mutableListOf<DmChannel>()
    private val mDMchennelAdapter = DmChannelSelectorAdapter(mDMchannelList)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dmchannel_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view_dmchannels_list.layoutManager = LinearLayoutManager(view.context)
        view_dmchannels_list.adapter = mDMchennelAdapter

        mChatVM.presenceUpdateLV.observe(viewLifecycleOwner, Observer {
            mDMchennelAdapter.notifyDataSetChanged()
        })

        mChatVM.dmChannelLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                mDMchannelList.clear()
                mDMchannelList.addAll(it)
                mDMchannelList.sortByDescending { item ->
                    item.lastMessageId
                }
                mDMchennelAdapter.notifyDataSetChanged()
            }
        })
        Client.global.eventBus.observeEventsOnUi().subscribe(
            Consumer { event ->
                if (event is MessageCreateEvent) {
                    mChatVM.loadDmChannel()
                } else if (event is MessageDeleteEvent) {
                    mChatVM.loadDmChannel()
                }
            })
        mChatVM.dmUnReadLiveData.observe(viewLifecycleOwner, Observer { map ->
            map.keys.forEach { key ->
                mDMchannelList.find { dmChannel ->
                    dmChannel.id == key
                }?.unReadCount = map[key] ?: 0
            }
            mDMchennelAdapter.notifyDataSetChanged()
        })

        mChatVM.mChannelCreateLD.observe(viewLifecycleOwner, Observer {
            if (it.channel is DmChannel) {
                mDMchennelAdapter.notifyItemInserted(mDMchannelList.size - 1)
            }
        })

        mChatVM.messageCreateLD.observe(viewLifecycleOwner, Observer { event ->
            if (event.message.guild == null || event.message.guild?.id == "@me") {
                mDMchannelList.sortByDescending { item ->
                    item.lastMessageId
                }
                mDMchennelAdapter.notifyDataSetChanged()
            }
        })
        mChatVM.loadDmChannel()
    }
}