package cn.troph.tomon.ui.chat.fragments

import android.content.Context
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
import cn.troph.tomon.ui.chat.viewmodel.DmChannelViewModel
import cn.troph.tomon.ui.states.AppState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_dmchannel_selector.*
import kotlinx.android.synthetic.main.fragment_guild_channel_selector.*

class DmChannelSelectorFragment : Fragment() {
    private val mDmchannelVM: DmChannelViewModel by viewModels()
    private lateinit var mAdapter: GuildSelectorAdapter

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
        mDmchannelVM.getChannelLiveData().observe(viewLifecycleOwner, Observer {
            it?.let {
                val mAdapter = DmChannelSelectorAdapter(it)
                view_dmchannels_list.layoutManager = LinearLayoutManager(view.context)
                view_dmchannels_list.adapter = mAdapter
            }
        })
        mDmchannelVM.loadDmChannel()
    }
}