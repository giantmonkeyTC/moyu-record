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
import cn.troph.tomon.core.network.Restful
import cn.troph.tomon.core.utils.Url
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.bottom_sheet_guild.view.*
import kotlinx.android.synthetic.main.fragment_guild_selector.*
import cn.troph.tomon.ui.widgets.UserAvatar
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
        btn_guild_fab.setOnClickListener{
            callBottomSheet()
        }
    }

    private fun callBottomSheet() {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_guild, null)
        val dialog = BottomSheetDialog(this.requireContext())
        dialog.setContentView(view)
        view.cancel_button.setOnClickListener {
            dialog.dismiss()
        }
        view.join_guild_button.setOnClickListener {
            Client.global.guilds.join(Url.parseInviteCode("https://beta.tomon.co/invite/FQmCup"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { guild ->
                        if (guild == null)
                            println("joined guild") else
                            println(guild.name)
                    }, { error -> println(error) }, { println("done") }
                )
        }
        dialog.show()
    }

}