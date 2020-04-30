package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection

class GuildSelectorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guild_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<RecyclerView>(R.id.view_guilds)
        list.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = GuildSelectorAdapter()
        }
    }

}