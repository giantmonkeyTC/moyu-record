package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.widgets.UserAvatar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

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
        val avatar = view.findViewById<UserAvatar>(R.id.view_avatar)
        avatar.userId = Client.global.me.id
        avatar.url = Client.global.me.avatarURL
        println(Client.global.me.avatarURL)
        Observable.create(Client.global.me).observeOn(AndroidSchedulers.mainThread()).subscribe {
            avatar.userId = Client.global.me.id
            avatar.url = Client.global.me.avatarURL
            println(Client.global.me.avatarURL)
        }
    }

}