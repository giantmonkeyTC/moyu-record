package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.events.MessageDeleteEvent
import cn.troph.tomon.core.events.MessageReadEvent
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.viewmodel.DmChannelViewModel
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.fragment_dmchannel_selector.*


class DmChannelSelectorFragment : Fragment() {
    private val mDmchannelVM: DmChannelViewModel by viewModels()
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
        val headerText = view?.findViewById<TextView>(R.id.text_channel_header_text)
        headerText?.text = "私聊"

        view_dmchannels_list.layoutManager = LinearLayoutManager(view.context)
        view_dmchannels_list.adapter = mDMchennelAdapter

        mDmchannelVM.getChannelLiveData().observe(viewLifecycleOwner, Observer {
            it?.let {
                mDMchannelList.clear()
                mDMchannelList.addAll(it)
                mDMchennelAdapter.notifyDataSetChanged()

            }
        })

        mDmchannelVM.loadDmChannel()

        Client.global.eventBus.observeEventOnUi<MessageReadEvent>().subscribe(Consumer {
            if (it.message.guild == null || it.message.guild?.id == "@me") {
                for ((index, value) in mDMchannelList.withIndex()) {
                    if (value.id == it.message.channelId) {
                        value.unReadCount = 0
                        mDMchennelAdapter.notifyItemChanged(index)
                    }
                }
            }
        })

        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>().subscribe(Consumer {
            if (it.message.guild == null || it.message.guild?.id == "@me") {
                for ((index, value) in mDMchannelList.withIndex()) {
                    if (value.id == it.message.channelId) {
                        value.unReadCount++
                        mDMchennelAdapter.notifyItemChanged(index)
                    }
                }
            }
        })

    }
}