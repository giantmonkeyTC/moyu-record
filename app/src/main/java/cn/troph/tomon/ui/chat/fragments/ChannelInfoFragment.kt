package cn.troph.tomon.ui.chat.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import at.blogc.android.views.ExpandableTextView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.Client.Companion.global
import cn.troph.tomon.core.MessageNotificationsType
import cn.troph.tomon.core.network.services.ChannelService
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.core.utils.DensityUtil
import cn.troph.tomon.core.utils.Url
import cn.troph.tomon.ui.chat.ui.ExpandNestedScrollView
import cn.troph.tomon.ui.chat.ui.NestedViewPager
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import cn.troph.tomon.ui.widgets.TomonToast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_channel_detail.*


class ChannelInfoFragment : Fragment() {
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: NestedViewPager
    private lateinit var tabLayout: TabLayout
    private val chatSharedViewModel: ChatSharedViewModel by activityViewModels()
    private var muteState = ChannelMuteState.DEFAULT
        set(value) {
            field = value
            when (value) {
                ChannelMuteState.DEFAULT -> {
                    channel_info_mute?.let {
                        channel_info_mute.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            requireContext().getDrawable(R.drawable.channel_info_mute),
                            null,
                            null
                        )
                        channel_info_mute.setTextColor(requireContext().getColor(R.color.whiteAlpha70))
                    }

                }
                ChannelMuteState.MUTED -> {
                    channel_info_mute?.let {
                        channel_info_mute.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            requireContext().getDrawable(R.drawable.channel_info_muted),
                            null,
                            null
                        )
                        channel_info_mute.setTextColor(requireContext().getColor(R.color.primaryColorAlpha80))
                    }
                }
            }
        }

    companion object {
        val actionBarMoveY = 55f
    }

    private var moveY = 0f
    private var moveX = 0f
    private var guildMoveX = 0f
    private var actionbarHeight = 0
    private var channelId: String? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed && value != null) {
                val channel = global.channels[value]
                if (channel is TextChannel)
                    muteState = when (global.guildSettings[channel.guildId
                        ?: ""]?.channelOverrides?.get(value)?.messageNotifications) {
                        MessageNotificationsType.DEFAULT -> ChannelMuteState.DEFAULT
                        MessageNotificationsType.ONLY_MENTION -> ChannelMuteState.MUTED
                        else -> ChannelMuteState.DEFAULT
                    }

            }

        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_detail, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val channelInfoDescription =
            view.findViewById<ExpandableTextView>(R.id.channel_info_description)
        val expandIcon = view.findViewById<ImageView>(R.id.ic_expand_text)
        val channelInfo = view.findViewById<TextView>(R.id.channel_info)

        channelInfoDescription.post {
            actionbarHeight = channel_info_actionbar.measuredHeight
            val layout = channelInfoDescription.layout
            val lines = layout.lineCount
            if (lines > 0) {
                val count = layout.getEllipsisCount(lines - 1)
                if (count == 0) {
                    space_expand.visibility = View.VISIBLE
                    expandIcon.visibility = View.GONE
                } else {
                    space_expand.visibility = View.GONE
                    expandIcon.visibility = View.VISIBLE
                }

            }
        }
        channel_info_scroll_view.setOnScrollListener(object :
            ExpandNestedScrollView.OnScrollListener {
            override fun onReset() {
                AppState.global.scrollPercent.value = 0f
            }
        })
        channel_info_scroll_view.setOnScrollChangeListener { v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY >= channel_info_header.height) {
                AppState.global.scrollPercent.value =
                    1f
            } else {
                AppState.global.scrollPercent.value =
                    scrollY.toFloat() / channel_info_header.height.toFloat()
            }

        }
        var point = Point()
        (requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(
            point
        )
        channel_info_invite
        channel_info_channel_name.maxWidth =
            point.x - DensityUtil.dip2px(context, 48f) * 2
        chatSharedViewModel.channelSelectionLD.observe(viewLifecycleOwner, Observer {
            channel_info_scroll_view.scrollTo(0, 0)
            channelId = it.channelId
            channelId?.let {
                val channel = global.channels[it]
                if (channel is TextChannel) {
                    channel_info_channel_name.text = channel.name
                    channel_info_guild_name.text = channel.guild?.name ?: "群组不存在"
                    if (channel.topic != "") {
                        expand_text.visibility = View.VISIBLE
                        channelInfoDescription.text = channel.topic
                    } else
                        expand_text.visibility = View.GONE
                }
            }
            channel_info_channel_name.isSelected = true
            channelInfoDescription.post {
                channelInfoDescription.collapse()
                expandIcon.setImageResource(R.drawable.channel_info_expand_arrow)
                moveY = DensityUtil.dip2px(context, actionBarMoveY).toFloat()
                moveX = imageView6.x - channelInfo.x
                guildMoveX = channel_info_guild_name.x - channelInfo.x
                val layout = channelInfoDescription.layout
                val lines = layout.lineCount
                if (lines > 0) {
                    val count = layout.getEllipsisCount(lines - 1)
                    if (count == 0) {
                        space_expand.visibility = View.VISIBLE
                        expandIcon.visibility = View.GONE
                    } else {
                        space_expand.visibility = View.GONE
                        expandIcon.visibility = View.VISIBLE
                    }

                }

            }
        })
        chatSharedViewModel.channelUpdateLD.observe(viewLifecycleOwner, Observer {
            if (it.channel.id == channelId && it.channel is TextChannel) {
                channel_info_channel_name.text = it.channel.name
                if (it.channel.topic != "") {
                    expand_text.visibility = View.VISIBLE
                    channelInfoDescription.text = it.channel.topic
                } else
                    expand_text.visibility = View.GONE
                channelInfoDescription.post {
                    val layout = channelInfoDescription.layout
                    val lines = layout.lineCount
                    if (lines > 0) {
                        val count = layout.getEllipsisCount(lines - 1)
                        if (count == 0) {
                            space_expand.visibility = View.VISIBLE
                            expandIcon.visibility = View.GONE
                        } else {
                            space_expand.visibility = View.GONE
                            expandIcon.visibility = View.VISIBLE
                        }

                    }
                }
                channelInfoDescription.post {
                    moveY = DensityUtil.dip2px(context, actionBarMoveY).toFloat()
                    moveX = imageView6.x - channelInfo.x
                    guildMoveX = channel_info_guild_name.x - channelInfo.x
                }
            }
        })
        chatSharedViewModel.guildUpdateLD.observe(viewLifecycleOwner, Observer { event ->
            channelId?.let {
                val channel = global.channels[it]
                if (channel is TextChannel) {
                    if (channel.guildId == event.guild.id) {
                        channel_info_guild_name.text = event.guild.name
                    }
                    channel_info_scroll_view.scrollTo(0, 0)
                    channelInfoDescription.post {
                        moveY = DensityUtil.dip2px(context, actionBarMoveY).toFloat()
                        moveX = imageView6.x - channelInfo.x
                        guildMoveX = channel_info_guild_name.x - channelInfo.x
                    }
                }
            }
        })
        channelInfoDescription.setInterpolator(OvershootInterpolator())
        AppState.global.scrollPercent.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it > 1f) {
                    channelInfo.alpha = 0f
                    channel_info_channel_name.translationY = moveY.unaryMinus()
                    channel_info_channel_name.translationX = moveX.unaryMinus()
                    imageView6.translationY = moveY.unaryMinus()
                    imageView6.translationX = moveX.unaryMinus()
                    channel_info_guild_name.translationY = moveY.unaryMinus()
                    channel_info_guild_name.translationX = guildMoveX.unaryMinus()
                    channel_info_actionbar.updateLayoutParams {
                        this.height = actionbarHeight - moveY.toInt()
                    }
                } else {
                    channelInfo.alpha = 1 - 2 * it
                    channel_info_channel_name.translationY = (it * moveY).unaryMinus()
                    channel_info_channel_name.translationX = (it * moveX).unaryMinus()
                    imageView6.translationY = (it * moveY).unaryMinus()
                    imageView6.translationX = (it * moveX).unaryMinus()
                    channel_info_guild_name.translationY = (it * moveY).unaryMinus()
                    channel_info_guild_name.translationX = (it * guildMoveX).unaryMinus()
                    channel_info_actionbar.updateLayoutParams {
                        this.height = actionbarHeight - (it * moveY).toInt()
                    }
                }

            }
        channel_info_invite.setOnClickListener {
            channelId?.let {
                val channel = global.channels[it]
                if (channel is TextChannel) {
                    var inviteCode: String = ""
                    var ticketCode: String = ""
                    Observable.merge(
                        Client.global.rest.inviteService.getChannelInvite(
                            channelId = it,
                            token = Client.global.auth
                        ).doOnNext {
                            inviteCode = it.code
                        }.doOnError { Toast.makeText(context, "获取频道邀请码失败", Toast.LENGTH_SHORT) },
                        Client.global.rest.inviteService.getTickets(token = Client.global.auth)
                            .doOnNext {
                                ticketCode = it.code
                            }.doOnError {
                                Toast.makeText(context, "获取激活码失败", Toast.LENGTH_SHORT)
                            }
                    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (inviteCode != "" && ticketCode != "") {
                                val inviteLink = Url.inviteFormat.format(inviteCode, ticketCode)
                                GeneralSnackbar.make(
                                    GeneralSnackbar.findSuitableParent(view)!!,
                                    getString(R.string.copied_invite),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                val clipboard =
                                    view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip: ClipData =
                                    ClipData.newPlainText("copy", inviteLink)
                                clipboard.setPrimaryClip(clip)
                                val intent = Intent().apply {
                                    setAction(Intent.ACTION_SEND)
                                    setType("text/plain")
                                    putExtra(Intent.EXTRA_TEXT, inviteLink)
                                }
                                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                                    startActivity(intent)
                                }

                            }
                        }


                }
            }
        }
        channel_info_mute.setOnClickListener {
            channelId?.let {
                val channel = global.channels[it]
                if (channel is TextChannel) {
                    when (muteState) {
                        ChannelMuteState.DEFAULT -> {
                            val array = JsonArray()
                            global.rest.channelService.setNotifyChannel(
                                channel.guildId ?: "",
                                JsonParser.parseString(
                                    Gson().toJson(
                                        ChannelService.NotifyChannelSetting(
                                            channel_overrides = listOf(
                                                ChannelService.ChannelOverride(
                                                    channelId = it,
                                                    messageNotifications = 1
                                                )
                                            )
                                        )
                                    )
                                ).asJsonObject,
                                global.auth
                            )
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe({
                                    muteState = ChannelMuteState.MUTED
                                }
                                ) {
                                    TomonToast.makeText(
                                        requireContext().applicationContext,
                                        getString(R.string.setting_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        ChannelMuteState.MUTED -> {
                            global.rest.channelService.setNotifyChannel(
                                channel.guildId ?: "",
                                JsonParser.parseString(
                                    Gson().toJson(
                                        ChannelService.NotifyChannelSetting(
                                            channel_overrides = listOf(
                                                ChannelService.ChannelOverride(
                                                    channelId = it,
                                                    messageNotifications = 0
                                                )
                                            )
                                        )
                                    )
                                ).asJsonObject,
                                global.auth
                            )
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe({
                                    muteState = ChannelMuteState.DEFAULT
                                }
                                ) {
                                    TomonToast.makeText(
                                        requireContext().applicationContext,
                                        getString(R.string.setting_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }

            }

        }
        expand_text.setOnClickListener {
            if (channelInfoDescription.isExpanded) {
                channelInfoDescription.collapse()
                expandIcon.setImageResource(R.drawable.channel_info_expand_arrow)
            } else {
                channelInfoDescription.expand()
                expandIcon.setImageResource(R.drawable.channel_info_collapse_arrow)
            }
        }
        viewPagerAdapter =
            ViewPagerAdapter(requireFragmentManager())
        viewPager = view.findViewById(R.id.channel_info_viewpager)
        viewPager.adapter = viewPagerAdapter
        tabLayout = view.findViewById(R.id.channel_info_tab)
//        tabLayout.addTab(tabLayout.newTab().apply {
//            this.
//            text = "成员"
//        })
//        tabLayout.addTab(tabLayout.newTab().apply {
//            text = "@我"
//        })
//        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//
//            }
//
//            override fun onPageSelected(position: Int) {
//                tabLayout.getTabAt(position)?.select()
//            }
//
//            override fun onPageScrollStateChanged(state: Int) {
//                when (state) {
//                    ViewPager.SCROLL_STATE_IDLE -> {
//                        Logger.d("idle")
//                    }
//                    ViewPager.SCROLL_STATE_DRAGGING -> {
//                        Logger.d("drag")
//                    }
//                    ViewPager.SCROLL_STATE_SETTLING -> {
//                        Logger.d("settle")
//                    }
//                }
//            }
//
//        })
        tabLayout.setupWithViewPager(viewPager)
        chatSharedViewModel.channelSelectionLD.observe(viewLifecycleOwner, Observer {
            channelId = it.channelId
        })

    }
}

enum class ChannelMuteState(val value: Int) {
    DEFAULT(0),
    MUTED(1)
}

class ViewPagerAdapter(
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager) {
    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "成员"
//            1 -> "@我"
            else -> ""
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val channelMemberFragment = ChannelMemberFragment()
                return channelMemberFragment
            }
//            1 -> {
//                val mentionMefragment = MentionMeFragment()
//                return mentionMefragment
//            }
            else -> Fragment()
        }

    }

    override fun getCount(): Int {
        return 1
    }

}