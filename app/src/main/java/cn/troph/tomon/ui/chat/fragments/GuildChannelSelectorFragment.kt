package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R

class GuildChannelSelectorFragment : Fragment() {

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
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = GuildChannelSelectorAdapter()
        }
    }

//    fun setOnSelectGuild(listener: ((guildId: String?) -> Unit)?) {
//        onSelectGuild = listener
//        val list = view?.findViewById<RecyclerView>(R.id.view_guilds)
//        list?.let {
//            (it.adapter as GuildSelectorAdapter).setOnGuildClickListener(listener)
//        }
//    }
}