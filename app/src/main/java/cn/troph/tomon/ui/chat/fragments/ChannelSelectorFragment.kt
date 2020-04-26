package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.troph.tomon.R

class ChannelSelectorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_channel_selector, container, false)
        val guildFragment = childFragmentManager.findFragmentById(R.id.fragment_guilds) as? GuildSelectorFragment
//        guildFragment?.setOnSelectGuild {
//            println(it)
//        }
        return view
    }
}