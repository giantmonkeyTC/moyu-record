package cn.troph.tomon.ui.chat.fragments


import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.*
import cn.troph.tomon.core.events.MessageAtMeEvent
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.events.MessageDeleteEvent
import cn.troph.tomon.core.events.MessageReadEvent
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.utils.BadgeUtil
import cn.troph.tomon.core.utils.Url
import cn.troph.tomon.core.utils.event.observeEvent
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.emoji.SystemEmoji
import cn.troph.tomon.ui.chat.viewmodel.GuildViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.annotations.SerializedName
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_guild_selector.*
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.bottom_sheet_join_guild.view.*

class GuildSelectorFragment : Fragment() {

    private val mGuildVM: GuildViewModel by viewModels()
    private val mGuildList = mutableListOf<Guild>()
    private val mAdapter = GuildSelectorAdapter(mGuildList)
    val guildChannelFragment: Fragment = GuildChannelSelectorFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.d(SystemEmoji().returnEmojiWithCategory().toString())
        return inflater.inflate(R.layout.fragment_guild_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mGuildVM.getGuildListLiveData().observe(viewLifecycleOwner, Observer {
            it?.let { list ->
                mGuildList.clear()
                mGuildList.addAll(list)
                mAdapter.setOnItemClickListener(object : GuildSelectorAdapter.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        val transaction =
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                replace(R.id.fragment_guild_channels, guildChannelFragment)
                                addToBackStack(null)
                            }
                        transaction.commit()
                    }
                })
                view_guilds.layoutManager = LinearLayoutManager(view.context)
                view_guilds.adapter = mAdapter
                it.forEach {
                    it.updateMention()
                    it.updateUnread()
                    mAdapter.notifyItemChanged(mGuildVM.getGuildListLiveData().value?.indexOf(it)!!)
                }
            }
        })
        mGuildVM.loadGuildList()
        view_avatar.user = Client.global.me
        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>()
            .subscribe(Consumer { event ->
                if (mGuildVM.getGuildListLiveData().value?.contains(event.message.guild)!!) {
                    if (event.message.guild!!.updateUnread()) {
                        mAdapter.notifyItemChanged(
                            mGuildVM.getGuildListLiveData().value!!.indexOf(
                                event.message.guild!!
                            )
                        )
                    }
                }
                if (event.message.guild == null || event.message.guild?.id == "@me") {
                    for ((index, value) in Client.global.dmChannels.withIndex()) {
                        if (value.id == event.message.channelId) {
                            BadgeUtil.increaseChannelUnread(value.id)
                            updateRedDot(BadgeUtil.getTotalUnread())
                        }
                    }
                }
            })
        Client.global.eventBus.observeEventOnUi<MessageReadEvent>()
            .subscribe(Consumer { event ->
                if (mGuildVM.getGuildListLiveData().value?.contains(event.message.guild)!!) {
                    if (!event.message.guild!!.updateUnread())
                        mAdapter.notifyItemChanged(
                            mGuildVM.getGuildListLiveData().value!!.indexOf(
                                event.message.guild!!
                            )
                        )
                    if (event.message.guild!!.updateMention())
                        mAdapter.notifyItemChanged(
                            mGuildVM.getGuildListLiveData().value!!.indexOf(
                                event.message.guild!!
                            )
                        )
                }
                if (event.message.guild == null || event.message.guild?.id == "@me") {
                    for ((index, value) in Client.global.dmChannels.withIndex()) {
                        if (value.id == event.message.channelId) {
                            BadgeUtil.clearChannelReadCount(value.id)
                            updateRedDot(BadgeUtil.getTotalUnread())
                        }
                    }
                }
            })
        Client.global.eventBus.observeEventOnUi<MessageAtMeEvent>().subscribe(Consumer { event ->
            if (mGuildVM.getGuildListLiveData().value?.contains(event.message.guild!!)!!) {
                if (event.message.guild!!.updateMention())
                    mAdapter.notifyItemChanged(
                        mGuildVM.getGuildListLiveData().value!!.indexOf(
                            event.message.guild!!
                        )
                    )
            }
        })
        Client.global.eventBus.observeEventOnUi<MessageDeleteEvent>().subscribe(Consumer { event ->
            if (mGuildVM.getGuildListLiveData().value?.contains(event.message.guild)!!) {
                if (event.message.guild!!.updateUnread()) {
                    mAdapter.notifyItemChanged(
                        mGuildVM.getGuildListLiveData().value!!.indexOf(
                            event.message.guild!!
                        )
                    )
                }
            }
        })
        Client.global.eventBus.observeEventOnUi<MessageUpdateEvent>().subscribe(
            Consumer { event ->
                event.message.guild?.let {
                    if (it.updateMention()) {
                        mAdapter.notifyItemChanged(
                            mGuildVM.getGuildListLiveData().value!!.indexOf(
                                event.message.guild!!
                            )
                        )
                    }
                }
            })

        Client.global.eventBus.observeEventOnUi<ChannelCreateEvent>().subscribe(Consumer {
            if (it.channel is DmChannel) {
                BadgeUtil.setChannelUnreadCount(it.channel.id, it.channel.unReadCount)
                updateRedDot(BadgeUtil.getTotalUnread())
            }
        })

        Client.global.eventBus.observeEventOnUi<GuildPositionEvent>().subscribe {
            val rearrangedGuildList = mutableListOf<Guild>()
            for (item in it.guilds) {
                val newGuild = mGuildList.find {
                    it.id == item.id
                }
                newGuild?.let {
                    rearrangedGuildList.add(item.position, it)
                }
            }
            mGuildList.clear()
            mGuildList.addAll(rearrangedGuildList)
            mAdapter.notifyDataSetChanged()
        }

        Client.global.eventBus.observeEventOnUi<GuildCreateEvent>().subscribe {
            mGuildList.add(it.guild)
            mAdapter.notifyItemInserted(mGuildList.size - 1)
        }

        Client.global.eventBus.observeEventOnUi<GuildDeleteEvent>().subscribe { event ->
            mGuildList.removeIf {
                it.id == event.guild.id
            }
            mAdapter.notifyDataSetChanged()
        }

        view_avatar.setOnClickListener {
            val userInfoBottomSheet = UserInfoFragment()
            userInfoBottomSheet.show(parentFragmentManager, userInfoBottomSheet.tag)
        }
        btn_dm_channel_entry.setOnClickListener {
            AppState.global.channelSelection.value =
                ChannelSelection(guildId = "@me", channelId = null)
            val dmChannelFragment: Fragment = DmChannelSelectorFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_guild_channels, dmChannelFragment)
                addToBackStack(null)
            }
            transaction.commit()
        }
        btn_join_guild.setOnClickListener {
            callJoinGuildBottomSheet()
        }

        var totalUnread = 0
        for (item in Client.global.dmChannels) {
            totalUnread += item.unReadCount
            BadgeUtil.setChannelUnreadCount(item.id, item.unReadCount)
        }
        updateRedDot(BadgeUtil.getTotalUnread())
    }

    private fun updateRedDot(number: Int) {
        if (number > 99) {
            dm_read_count.visibility = View.VISIBLE
            dm_read_count.text = "..."
        }
        if (number > 0) {
            dm_read_count.visibility = View.VISIBLE
            dm_read_count.text = number.toString()
        } else {
            dm_read_count.visibility = View.GONE
        }
    }

    private fun callJoinGuildBottomSheet() {
        val layoutInflater = LayoutInflater.from(requireContext())
        val viewBase = layoutInflater.inflate(R.layout.coordinator_join_guild, null)
        val view = viewBase.bottom_sheet_join_guild
        val textField = view.findViewById<EditText>(R.id.bs_textfield)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(viewBase)
        view.cancel.setOnClickListener { dialog.dismiss() }
        view.confirm.setOnClickListener {
            if (textField.text.toString().matches(Regex("[A-Za-z0-9]+"))) {
                Client.global.guilds.fetchInvite(
                    if (textField.text.toString().contains(Url.inviteUrl))
                        Url.parseInviteCode(textField.text.toString())
                    else textField.text.toString()
                ).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    if (it != null) {
                        val invite = it
                        if (invite.joined) {
                            GeneralSnackbar.make(
                                GeneralSnackbar.findSuitableParent(viewBase)!!,
                                "你已经在该群组中",
                                Snackbar.LENGTH_LONG
                            ).show()
                            textField.setText("")
                        } else {
                            Client.global.guilds.join(
                                if (textField.text.toString().contains(Url.inviteUrl))
                                    Url.parseInviteCode(textField.text.toString())
                                else textField.text.toString()
                            )
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    { guild ->
                                        if (guild != null) {
                                            dialog.dismiss()
                                        }
                                    }, { error -> println(error) }, { }
                                )
                        }
                    }
                }

            }
        }
        dialog.show()
    }
}

data class GuildInvite(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("memberCount")
    val memberCount: Int,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("iconUrl")
    val iconUrl: String
)

data class Inviter(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("discriminator")
    val discriminator: String,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar_url")
    val avatar_url: String
)

data class Invite(
    @SerializedName("code")
    val code: String,
    @SerializedName("guild")
    val guild: GuildInvite,
    @SerializedName("inviter")
    val inviter: Inviter,
    @SerializedName("joined")
    val joined: Boolean
)