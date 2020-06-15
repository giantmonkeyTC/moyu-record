package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.chat.viewmodel.DmChannelViewModel
import cn.troph.tomon.ui.states.AppState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import androidx.lifecycle.Observer
import java.util.*

class GuildChannelSelectorFragment : Fragment() {
    private val mDmchannelVM: DmChannelViewModel by viewModels()
    var disposable: Disposable? = null

    var guildId: String? = null
        set(value) {
            field = value
            update()
            mDmchannelVM.loadDmChannel()
            val guild = guildId?.let { Client.global.guilds[it] }
            disposable?.dispose()
            disposable = guild?.observable?.observeOn(AndroidSchedulers.mainThread())?.subscribe {
                update()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guild_channel_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<RecyclerView>(R.id.view_guild_channels)
        if (guildId == "@me")
            mDmchannelVM.getChannelLiveData().observe(viewLifecycleOwner, Observer {
                it?.let {
                    val mAdapter = DmChannelSelectorAdapter(it)
                    list.layoutManager = LinearLayoutManager(view.context)
                    list.adapter = mAdapter
                }
            })
        else
            list.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = GuildChannelSelectorAdapter().apply { hasStableIds() }
            }
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe { guildId = it.guildId }
    }

    fun update() {
        val guild = guildId?.let { Client.global.guilds[it] }
        val headerText = view?.findViewById<TextView>(R.id.text_channel_header_text)
        if (guildId == "@me")
            headerText?.text = "私聊"
        else
            headerText?.text = guild?.name
    }

}