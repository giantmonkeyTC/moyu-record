package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelCreateEvent
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.messages.notifyObserver
import cn.troph.tomon.ui.chat.viewmodel.DmChannelViewModel
import cn.troph.tomon.ui.chat.viewmodel.UnReadViewModel
import cn.troph.tomon.ui.states.AppState
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.fragment_dmchannel_selector.*
import java.util.function.BiFunction


class DmChannelSelectorFragment : Fragment() {
    private val mDmchannelVM: DmChannelViewModel by viewModels()
    private val mDMchannelList = mutableListOf<DmChannel>()
    private val mDMchennelAdapter = DmChannelSelectorAdapter(mDMchannelList)
    private val mUnReadViewModel: UnReadViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dmchannel_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val headerText = view.findViewById<TextView>(R.id.text_channel_header_text)
        headerText?.text = "私聊"

        view_dmchannels_list.layoutManager = LinearLayoutManager(view.context)
        view_dmchannels_list.adapter = mDMchennelAdapter

        mDmchannelVM.getChannelLiveData().observe(viewLifecycleOwner, Observer {
            it?.let {
                mDMchannelList.clear()
                mDMchannelList.addAll(it)
                mDMchannelList.sortByDescending { item ->
                    item.lastMessageId
                }
                mDMchennelAdapter.notifyDataSetChanged()
            }
        })

        mDmchannelVM.loadDmChannel()

        mUnReadViewModel.dmUnReadLiveData.observe(viewLifecycleOwner, Observer { map ->
            map.keys.forEach { key ->
                mDMchannelList.find { dmChannel ->
                    dmChannel.id == key
                }?.unReadCount = map[key] ?: 0
            }
            mDMchennelAdapter.notifyDataSetChanged()
        })

        Client.global.eventBus.observeEventOnUi<ChannelCreateEvent>().subscribe(Consumer {
            if (it.channel is DmChannel) {
                mDMchannelList.add(it.channel)
                mDMchennelAdapter.notifyItemInserted(mDMchannelList.size - 1)
            }
        })

        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>().subscribe(Consumer { event ->
            if (event.message.guild == null || event.message.guild?.id == "@me") {
                mDMchannelList.sortByDescending { item ->
                    item.lastMessageId
                }


                if (event.message.authorId != Client.global.me.id) {
                    mUnReadViewModel.dmUnReadLiveData.value?.computeIfPresent(event.message.channelId,
                        BiFunction { t, u ->
                            u.inc()
                        })
                    mUnReadViewModel.dmUnReadLiveData.notifyObserver()
                    return@Consumer
                }
                mDMchennelAdapter.notifyDataSetChanged()
            }
        })
    }
}