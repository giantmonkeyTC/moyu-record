package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.ui.states.AppState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class GuildChannelSelectorFragment : Fragment() {

    var guildId: String? = null
        set(value) {
            field = value
            val guild = if (value != null) Client.global.guilds[value] else null
            if (guild != null) {
                Observable.create(guild).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    val headerText = view?.findViewById<TextView>(R.id.text_channel_header_text)
                    headerText?.text = guild.name
                }
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
        list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GuildChannelSelectorAdapter().apply { hasStableIds() }
        }
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe { guildId = it.guildId }
    }

}