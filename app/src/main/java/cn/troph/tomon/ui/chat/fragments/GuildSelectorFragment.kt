package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.Url
import cn.troph.tomon.ui.chat.viewmodel.GuildViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_guild_selector.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.bottom_sheet_join_guild.view.*

class GuildSelectorFragment : Fragment() {

    private val mGuildVM: GuildViewModel by viewModels()
    private lateinit var mAdapter: GuildSelectorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guild_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mGuildVM.getGuildListLiveData().observe(viewLifecycleOwner, Observer {
            it?.let {
                mAdapter = GuildSelectorAdapter(it)
                view_guilds.layoutManager = LinearLayoutManager(view.context)
                view_guilds.adapter = mAdapter
            }
        })
        mGuildVM.loadGuildList()
        view_avatar.setOnClickListener {
            val user_info_bottomsheet = UserInfoFragment()
            user_info_bottomsheet.show(parentFragmentManager, user_info_bottomsheet.tag)
        }
        btn_guild_fab.setOnClickListener {
            callJoinGuildBottomSheet()
        }
    }

    private fun callJoinGuildBottomSheet() {
        val layoutInflater = LayoutInflater.from(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_join_guild, null)
        val textField = view.findViewById<EditText>(R.id.bs_textfield)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        view.cancel.setOnClickListener { dialog.dismiss() }
        view.confirm.setOnClickListener {
            if (textField.text.toString().matches(Regex("[A-Za-z0-9]+"))) {
                Client.global.guilds.fetchInvite(
                    if (textField.text.toString().contains(Url.inviteUrl))
                        Url.parseInviteCode(textField.text.toString())
                    else textField.text.toString()
                ).observeOn(AndroidSchedulers.mainThread()).subscribe {


//                    if (it != null) {
//                        val guildRaw = it["guild"].asJsonObject
//                        val inviterRaw = it["inviter"].asJsonObject
//                        val guild = GuildInvite(
//                            id = guildRaw["id"].asString,
//                            name = guildRaw["name"].asString,
//                            memberCount = guildRaw["member_count"].asInt,
//                            icon = guildRaw["icon"].asString,
//                            iconUrl = guildRaw["icon_url"].asString
//                        )
//                        val inviter = Inviter(
//                            id = inviterRaw["id"].asString,
//                            username = inviterRaw["username"].asString,
//                            discriminator = inviterRaw["discriminator"].asString,
//                            avatar = inviterRaw["avatar"].asString,
//                            avatarUrl = inviterRaw["avatar_url"].asString,
//                            name = inviterRaw["name"].asString
//                        )
//                        val invite = Invite(
//                            code = it["code"].asString,
//                            guild = guild,
//                            inviter = inviter,
//                            joined = it["joined"].asBoolean
//                        )
//                        if (invite.joined) {
//                            textField.setText("")
//
//                        } else {
//                            Client.global.guilds.join(
//                                if (textField.text.toString().contains(Url.inviteUrl))
//                                    Url.parseInviteCode(textField.text.toString())
//                                else textField.text.toString()
//                            )
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(
//                                    { guild ->
//                                        if (guild != null) {
//                                            dialog.dismiss()
//                                        }
//                                    }, { error -> println(error) }, { }
//                                )
//                        }
//                    }
                }

            }
        }
        dialog.show()
    }

//    class JoinGuildDialog : DialogFragment() {
//        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//            val inflater = requireActivity().layoutInflater
//            val view = inflater.inflate(R.layout.bottom_sheet_join_guild, null)
//            val dialog = Dialog(requireActivity())
//            dialog.setContentView(view)
//            dialog.setCanceledOnTouchOutside(true)
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            return dialog
//        }
//
//
//    }

//            Client.global.guilds.join(Url.parseInviteCode("https://beta.tomon.co/invite/FQmCup"))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                    { guild ->
//                        if (guild == null)
//                            println("joined guild") else
//                            println(guild.name)
//                    }, { error -> println(error) }, { println("done") }
//                )


}

data class GuildInvite(
    val id: String,
    val name: String,
    val memberCount: Int,
    val icon: String,
    val iconUrl: String
)

data class Inviter(
    val id: String,
    val username: String,
    val discriminator: String,
    val avatar: String,
    val name: String,
    val avatarUrl: String
)

data class Invite(
    val code: String,
    val guild: GuildInvite,
    val inviter: Inviter,
    val joined: Boolean
)