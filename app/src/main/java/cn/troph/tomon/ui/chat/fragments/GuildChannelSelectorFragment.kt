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
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.chat.viewmodel.GuildChannelViewModel
import cn.troph.tomon.ui.states.AppState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_guild_channel_selector.*

class GuildChannelSelectorFragment : Fragment() {

    private val mGuildChannelViewModel: GuildChannelViewModel by viewModels()
    private lateinit var mGuildChannelAdapter: GuildChannelSelectorAdapter

    var disposable: Disposable? = null

    var guildId: String? = null
        set(value) {
            field = value
            update()
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
        mGuildChannelViewModel.getChannelLiveData().observe(viewLifecycleOwner, Observer {
            view_guild_channels.layoutManager = LinearLayoutManager(view.context)
            mGuildChannelAdapter = GuildChannelSelectorAdapter(it)
            view_guild_channels.adapter = mGuildChannelAdapter
        })
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                guildId = it.guildId
                guildId?.let {
                    mGuildChannelViewModel.loadGuildChannel(it)
                }

            }
    }

    fun update() {
        val guild = guildId?.let { Client.global.guilds[it] }
        val headerText = view?.findViewById<TextView>(R.id.text_channel_header_text)
        headerText?.text = guild?.name
    }

}