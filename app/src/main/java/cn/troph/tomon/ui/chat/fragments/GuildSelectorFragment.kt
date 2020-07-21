package cn.troph.tomon.ui.chat.fragments


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.utils.Url
import cn.troph.tomon.ui.chat.messages.notifyObserver
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.chat.viewmodel.GuildViewModel
import cn.troph.tomon.ui.chat.viewmodel.UnReadViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_guild_selector.*
import kotlinx.android.synthetic.main.bottom_sheet_join_guild.view.*

class GuildSelectorFragment : Fragment() {

    private val mGuildVM: GuildViewModel by viewModels()
    private val mChatVM: ChatSharedViewModel by activityViewModels()
    private val mUnReadViewModel: UnReadViewModel by activityViewModels()
    private val mGuildList = mutableListOf<Guild>()
    private val mAdapter = GuildSelectorAdapter(mGuildList)
    private var mSelectedGuild: Guild? = null
    private var isLastDmchannel: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guild_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mChatVM.setUpChannelSelection()
        requireActivity().supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragment_guild_channels, GuildChannelSelectorFragment())
                addToBackStack(null)
            }.commit()

        mChatVM.channelSelectionLD.observe(viewLifecycleOwner, Observer { channel ->
            if (channel.guildId.equals("@me", true) || channel.guildId == mSelectedGuild?.id)
                return@Observer

            val oldIndex = mGuildList.indexOfFirst {
                it.isSelected
            }

            var newIndex = 0
            mGuildList.forEach {
                it.isSelected = false
                if (it.id == channel.guildId) {
                    mSelectedGuild = it
                    it.isSelected = true
                    newIndex = mGuildList.indexOf(it)
                }
            }
            mAdapter.notifyItemChanged(oldIndex)
            mAdapter.notifyItemChanged(newIndex)

            if (isLastDmchannel)
                requireActivity().supportFragmentManager.beginTransaction()
                    .apply {
                        setCustomAnimations(
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right
                        )
                        replace(R.id.fragment_guild_channels, GuildChannelSelectorFragment())
                        addToBackStack(null)
                        isLastDmchannel = false
                    }.commit()
            btn_dm_channel_entry.isEnabled = true
        })

        mGuildVM.getGuildListLiveData().observe(viewLifecycleOwner, Observer {
            it?.let { list ->
                mGuildList.clear()
                mGuildList.addAll(list)

                mGuildList.forEach { singleGuild ->
                    singleGuild.isSelected = false
                    mSelectedGuild?.let {
                        if (it.id == singleGuild.id) {
                            singleGuild.isSelected = true
                        }
                    }
                }
                view_guilds.layoutManager = LinearLayoutManager(requireContext())
                view_guilds.adapter = mAdapter
                list.forEach {
                    it.updateMention()
                    it.updateUnread()
                    mAdapter.notifyItemChanged(mGuildVM.getGuildListLiveData().value?.indexOf(it)!!)
                }
            }
        })

        mGuildVM.loadGuildList()
        view_avatar.user = Client.global.me
        mGuildVM.messageCreateLD.observe(viewLifecycleOwner, Observer { event ->
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

        mGuildVM.messageReadLD.observe(viewLifecycleOwner, Observer { event ->
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
        })

        mGuildVM.messageAtMeLD.observe(viewLifecycleOwner, Observer { event ->
            if (mGuildVM.getGuildListLiveData().value?.contains(event.message.guild!!)!!) {
                if (event.message.guild!!.updateMention())
                    mAdapter.notifyItemChanged(
                        mGuildVM.getGuildListLiveData().value!!.indexOf(
                            event.message.guild!!
                        )
                    )
            }
        })

        mGuildVM.messageDeleteLD.observe(viewLifecycleOwner, Observer { event ->
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

        mGuildVM.messageUpdateLD.observe(viewLifecycleOwner, Observer { event ->
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

        mGuildVM.guildPositionLD.observe(viewLifecycleOwner, Observer {
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
        })
        mGuildVM.guildCreateLD.observe(viewLifecycleOwner, Observer {
            mGuildList.add(it.guild)
            mAdapter.notifyItemInserted(mGuildList.size - 1)
        })
        mGuildVM.guildDeleteLD.observe(viewLifecycleOwner, Observer { event ->
            mGuildList.removeIf {
                it.id == event.guild.id
            }
            mAdapter.notifyDataSetChanged()
        })

        view_avatar.setOnClickListener {
            val userInfoBottomSheet = UserInfoFragment()
            userInfoBottomSheet.show(parentFragmentManager, userInfoBottomSheet.tag)
        }
        btn_dm_channel_entry.setOnClickListener {
            AppState.global.channelSelection.value =
                ChannelSelection(guildId = "@me", channelId = null)
            val dmChannelFragment: Fragment = DmChannelSelectorFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction().apply {
                setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                replace(R.id.fragment_guild_channels, dmChannelFragment)
                addToBackStack(null)
            }
            transaction.commit()
            btn_dm_channel_entry.isEnabled = false
            isLastDmchannel = true
        }
        btn_join_guild.setOnClickListener {
            callJoinGuildBottomSheet()
        }

        mGuildVM.setUpEventBus()
        mUnReadViewModel.dmUnReadLiveData.observe(viewLifecycleOwner, Observer {
            updateRedDot(it.values.sum())
        })

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
        dialog.window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            ?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
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
                                            GeneralSnackbar.make(
                                                GeneralSnackbar.findSuitableParent(viewBase)!!,
                                                "加入成功",
                                                Snackbar.LENGTH_LONG
                                            ).show()
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