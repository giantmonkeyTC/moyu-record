package cn.troph.tomon.ui.chat.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.LinkParseReadyEvent
import cn.troph.tomon.core.events.MessageReadEvent
import cn.troph.tomon.core.network.NetworkConfigs
import cn.troph.tomon.core.network.services.MessageService
import cn.troph.tomon.core.structures.*
import cn.troph.tomon.core.utils.Assets
import cn.troph.tomon.core.utils.SnowFlakesGenerator
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.emoji.EmojiFragment
import cn.troph.tomon.ui.chat.emoji.OnEmojiClickListener
import cn.troph.tomon.ui.chat.emoji.ReactionFragment
import cn.troph.tomon.ui.chat.mention.MentionListAdapter
import cn.troph.tomon.ui.chat.messages.*
import cn.troph.tomon.ui.chat.ui.NestedViewPager
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.NetworkChangeReceiver
import cn.troph.tomon.ui.states.ReplyEnabled
import cn.troph.tomon.ui.states.UpdateEnabled
import coil.Coil
import coil.request.LoadRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import kotlinx.android.synthetic.main.fragment_channel_panel.view.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.data.UploadNotificationConfig
import net.gotev.uploadservice.data.UploadNotificationStatusConfig
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.time.LocalDateTime
import kotlin.random.Random

const val FILE_REQUEST_CODE_FILE = 323
const val LAST_CHANNEL_ID = "last_channel_id"
const val LAST_GUILD_ID = "last_guild_id"

class ChannelPanelFragment : BaseFragment() {
    private val mSwitchChannelMap = HashMap<String, Editable>()
    private val mChatSharedVM: ChatSharedViewModel by activityViewModels()
    private lateinit var mLayoutManager: LinearLayoutManager
    private val mHandler = Handler()
    private lateinit var mBottomSheet: FileBottomSheetFragment
    private lateinit var viewPagerCollectionAdapter: ViewPagerCollectionAdapter
    private lateinit var viewPager: NestedViewPager
    private val mIntentFilter = IntentFilter()
    private val mNetworkChangeReceiver = NetworkChangeReceiver()
    private lateinit var mEasyImage: EasyImage

    private val mEmojiClickListener = object : OnEmojiClickListener {
        override fun onEmojiSelected(emojiCode: String) {
            editText.text?.append(emojiCode)
        }

        override fun onSystemEmojiSelected(unicode: String) {
            editText.text?.append(unicode)
        }
    }

    private var mChannelId: String? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed && value != null) {
                btn_scroll_to_bottom.visibility = View.GONE
                mHeaderMsg.isEnd = false
                isFetchingMore = false
                editText?.let { input ->
                    input.text = mSwitchChannelMap[value]
                    input.setSelection(input.text.length)
                }
                val channel = Client.global.channels[value]
                val count = mMsgList.size
                mMsgList.clear()
                mMsgListAdapter.notifyItemRangeRemoved(0, count)
                if (channel is DmChannel) {
                    backgroundView?.visibility = View.GONE
                    mHeaderMsg.isGuild = false
                    channel.recipient?.let {
                        mHeaderMsg.channelText = it.name
                    }
                    Client.global.preferences.edit {
                        putString(LAST_CHANNEL_ID, value)
                    }
                    mChatSharedVM.loadDmChannelMessage(value)
                    editText?.let {
                        it.isEnabled = true
                        it.hint = getString(R.string.emoji_et_hint)
                    }
                    btn_message_menu?.let {
                        it.visibility = View.VISIBLE
                        it.isEnabled = true
                    }
                    emoji_tv?.let {
                        it.visibility = View.VISIBLE
                        it.isEnabled = true
                    }
                    btn_message_send?.let {
                        it.visibility = View.VISIBLE
                    }
                } else if (channel is TextChannel) {
                    mHeaderMsg.isGuild = true
                    mHeaderMsg.channelText = channel.name
                    Client.global.preferences.edit {
                        putString(LAST_GUILD_ID, (channel as TextChannel).guildId)
                        putString(LAST_CHANNEL_ID, value)
                    }
                    mChatSharedVM.loadTextChannelMessage(value)
                    channel.guild?.let { g ->
                        if (g.backgroundUrl.isNotEmpty()) {
                            val imageLoader = Coil.imageLoader(requireContext())
                            val request = LoadRequest.Builder(requireContext())
                                .data(g.backgroundUrl)
                                .target(
                                    onStart = { placeholder ->
                                        // Handle the placeholder drawable.
                                    },
                                    onSuccess = { result ->
                                        backgroundView?.setImageDrawable(result)
                                        backgroundView?.visibility = View.VISIBLE
                                    },
                                    onError = { error ->
                                        backgroundView?.visibility = View.GONE
                                    }
                                )
                                .build()
                            imageLoader.execute(request)

                        } else {
                            backgroundView?.visibility = View.INVISIBLE
                        }
                    }
                    if (channel.members[Client.global.me.id] == null) {
                        mChatSharedVM.mChannelMemberUpdateLD.observe(viewLifecycleOwner, Observer {
                            if (channel == it.channel) {
                                setNoPermissionViewIfNeeded(channel)
                            }
                        })
                    } else {
                        setNoPermissionViewIfNeeded(channel)
                    }
                }

            }
        }

    private fun setNoPermissionViewIfNeeded(channel: TextChannel) {
        val meCollectionNone = channel.members[Client.global.me.id]?.roles?.collection?.none {
            it.permissions.has(Permissions.SEND_MESSAGES)
        }

        val meCollectionAny = channel.members[Client.global.me.id]?.roles?.collection?.any {
            val po = channel.permissionOverwrites[it.id]
            if (po != null)
                po.denyPermission(Permissions.SEND_MESSAGES)!!
            else false
        }

        if (meCollectionNone!! || meCollectionAny!!) {
            editText?.let {
                it.hint = getString(R.string.ed_msg_hint_no_permisson)
                it.isEnabled = false
            }
            btn_message_menu?.let {
                it.visibility = View.GONE
                it.isEnabled = false
            }
            emoji_tv?.let {
                it.visibility = View.GONE
                it.isEnabled = false
            }
            btn_message_send?.let {
                it.visibility = View.GONE
            }
        } else {
            editText?.let {
                it.hint = getString(R.string.emoji_et_hint)
                editText.isEnabled = true
            }
            btn_message_menu?.let {
                it.visibility = View.VISIBLE
                it.isEnabled = true
            }
            emoji_tv?.let {
                it.visibility = View.VISIBLE
                it.isEnabled = true
            }
            btn_message_send?.let {
                it.visibility = View.VISIBLE
            }
        }
    }

    private var isUpdateEnabled: Boolean = false
        set(value) {
            field = value
            if (field) {
                editText.setText(message?.content)
                bar_update_message.visibility = View.VISIBLE
            } else {
                editText.text = null
                bar_update_message.visibility = View.GONE
            }
        }

    private var message: Message? = null
    private lateinit var mHeaderMsg: HeaderMessage
    private var isFetchingMore = false
    private val mMsgList = mutableListOf<Message>()
    private val mMsgListAdapter: MessageAdapter =
        MessageAdapter(mMsgList, object : ReactionSelectorListener {
            override fun OnReactionAddClicked(msg: Message) {
                val bs = ReactionFragment()
                bs.show(childFragmentManager, "reaction")
                bs.setMessage(msg)
            }
        }, object : OnAvatarLongClickListener {
            override fun onAvatarLongClick(identifier: String) {
                val start = editText.selectionStart
                editText.text.insert(
                    start,
                    mentionFormat(identifier)
                )
                editText.text.setSpan(
                    ForegroundColorSpan(requireContext().getColor(R.color.secondary)),
                    start,
                    start + mentionFormat(identifier).length,
                    0x11
                )

            }
        }, object : OnReplyClickListener {
            override fun onReplyClick(message: Message?) {
                mChatSharedVM.replyLd.value = ReplyEnabled(flag = true, message = message)
            }
            override fun onSourceClick(message: Message?, position: Int) {
                mMsgList.forEachIndexed { index, mes ->
                    if (message != null) {
                        if (message.id == mes.id)
                            view_messages.scrollToPosition(index)
                    }
                }
            }
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_panel, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val obj = JsonObject()
        obj.addProperty("id", "0")
        mHeaderMsg = HeaderMessage(Client.global, obj)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(mNetworkChangeReceiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mEasyImage = EasyImage.Builder(requireContext())
            .allowMultiple(false).build()
        viewPagerCollectionAdapter =
            ViewPagerCollectionAdapter(mEmojiClickListener, requireFragmentManager())
        viewPager = view.findViewById(R.id.reaction_stamp_viewpager)
        viewPager.adapter = viewPagerCollectionAdapter
        mChatSharedVM.updateLD.observe(viewLifecycleOwner, Observer {
            message = it.message
            isUpdateEnabled = it.flag
        })
        mChatSharedVM.replyLd.observe(viewLifecycleOwner, Observer {
            if (it.flag) {
                it.message?.let { mes ->
                    editText.setText("@${mes.author?.identifier}")
                }
                bar_reply_message.visibility = View.VISIBLE
            } else {
                bar_reply_message.visibility = View.GONE
            }
            bar_reply_message.message_reply_to.text =

                "${if ((it.message?.author?.name ?: "").length > 6) (it.message?.author?.name?.substring(
                    0,
                    6
                ) ?: "") + "···" else it.message?.author?.name ?: ""}:${it.message?.content ?: ""}"

        })
        bar_reply_message.btn_reply_message_cancel.setOnClickListener {
            mChatSharedVM.replyLd.value = ReplyEnabled(flag = false, message = null)
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    mChannelId?.let {
                        if (Client.global.channels[it] is TextChannel)
                            if (s.isNotEmpty() && start < s.length)
                                mChatSharedVM.mentionState.value =
                                    ChatSharedViewModel.MentionState(
                                        state = s[start].toString() == "@" && count == 1,
                                        start = start
                                    )
                            else
                                mChatSharedVM.mentionState.value =
                                    ChatSharedViewModel.MentionState(
                                        state = false,
                                        start = start
                                    )
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                //先注释掉，之后再上线bot功能
//                s?.let {
//                    if (it.toString().length >= 0 && it.toString().endsWith('/')) {
//                        BotCommandBottomSheet().show(parentFragmentManager, null)
//                    }
//                }
            }
        })
        mChatSharedVM.mentionState.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it.state) {
                    mChannelId?.let {
                        mChatSharedVM.loadMemberList(it)
                    }
                    hideKeyboard()
                }
            }
        })
        mChatSharedVM.memberLiveData.observe(viewLifecycleOwner, Observer {
            it.let {
                if (mChatSharedVM.mentionState.value?.state!!)
                    callMentionBottomSheet(it)
            }
        })
        mChatSharedVM.stampSendedLiveData.observe(viewLifecycleOwner, Observer {
            if (it.state) {
                mMsgList.add(it.emptyMsg)
                mMsgListAdapter.notifyItemInserted(mMsgList.size - 1)
                scrollToBottom()
            }

        })
        Client.global.eventBus.observeEventOnUi<LinkParseReadyEvent>().subscribe(Consumer {
            if (it.linkList.size > 0) {
                mMsgList.find { message ->
                    return@find message.id == it.linkList.first().messageId
                }.apply {
                    this?.let { mes ->
                        mes.links.clear()
                        mes.links.addAll(it.linkList)
                        mMsgList.forEachIndexed { index, message ->
                            if (message.id == mes.id) {
                                val position = index
                                mMsgListAdapter.notifyItemChanged(position)
                            }
                        }

                    }
                }
            }
        })

        mChatSharedVM.channelSelectionLD.observe(viewLifecycleOwner, Observer {
            if (it.channelId == null) {
                mChannelId = Client.global.preferences.getString(LAST_CHANNEL_ID, null)
            } else {

                mChannelId?.let {
                    mSwitchChannelMap[it] = editText.text
                }

                mChannelId = it.channelId
            }
        })

        mChatSharedVM.messageLoadingLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                shimmer_view_container.visibility = View.VISIBLE
                shimmer_view_container.startShimmer()
            } else {
                shimmer_view_container.visibility = View.GONE
                shimmer_view_container.stopShimmer()
            }
        })

        KeyboardVisibilityEvent.setEventListener(requireActivity(),
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (isOpen) {
                        btn_message_menu.isChecked = false
                        emoji_tv.isChecked = false
                        scrollToBottom()
                    }
                }
            })
        mNetworkChangeReceiver.setTopView(btn_message_send)
        mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        requireActivity().registerReceiver(mNetworkChangeReceiver, mIntentFilter)
        var longLastClickTime = 0L
        btn_message_send.setOnClickListener {
            if (SystemClock.elapsedRealtime() - longLastClickTime < 1000) {
                return@setOnClickListener
            }
            longLastClickTime = SystemClock.elapsedRealtime()
            if (mChannelId.isNullOrEmpty() || editText.text.isNullOrEmpty()) {
                return@setOnClickListener
            }
            val textToSend =
                if (Assets.regexMention.containsMatchIn(editText.text.toString()))
                    Assets.mentionSendParser(editText.text.toString())
                else
                    editText.text.toString()
            editText.text.clear()
            mSwitchChannelMap
            mChannelId?.let {
                mSwitchChannelMap.remove(it)
            }

            if (
                if (mChatSharedVM.replyLd.value != null)
                    mChatSharedVM.replyLd.value!!.flag else false
            ) {
                val emptyMsg = createEmptyMsg(textToSend)
                mMsgList.add(emptyMsg)
                mMsgListAdapter.notifyItemInserted(mMsgList.size - 1)
                scrollToBottom()
                Client.global.rest.messageService.createReplyMessage(
                    channelId = mChannelId ?: "",
                    jsonObject =
                    JsonParser.parseString(
                        Gson().toJson(
                            MessageService.replyParams(
                                nonce = emptyMsg.nonce.toString(),
                                content = textToSend,
                                reply = mChatSharedVM.replyLd.value!!.message?.id ?: ""
                            )
                        )
                    ).asJsonObject
                    ,
                    token = Client.global.auth
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                            _ -> mChatSharedVM.replyLd.value = ReplyEnabled(flag = false, message = null)
                    }, { error -> })
            } else if (!AppState.global.updateEnabled.value.flag) {
                //新建一个空message，并加入到RecyclerView
                val emptyMsg = createEmptyMsg(textToSend)
                mMsgList.add(emptyMsg)
                mMsgListAdapter.notifyItemInserted(mMsgList.size - 1)
                scrollToBottom()
                //发送消息
                (Client.global.channels[mChannelId
                    ?: ""] as TextChannelBase).messages.create(
                    textToSend,
                    nonce = emptyMsg.nonce.toString()
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ ->
                        mHandler.postDelayed({
                            emptyMsg.isSending = false;
                            mMsgListAdapter.notifyItemChanged(mMsgList.indexOf(emptyMsg))
                            scrollToBottom()
                        }, 300)
                    }
                        , { _ ->
                            Toast.makeText(requireContext(), R.string.send_fail, Toast.LENGTH_SHORT)
                                .show()
                        })
            } else {
                message!!.update(textToSend)
                    .observeOn(AndroidSchedulers.mainThread()).subscribe({ msg ->
                        AppState.global.updateEnabled.value =
                            UpdateEnabled(flag = false)
                        for ((index, value) in mMsgList.withIndex()) {
                            if (value.id == msg.id) {
                                mMsgListAdapter.notifyItemChanged(index)
                                break
                            }
                        }
                        scrollToBottom()
                    }, { _ ->
                        Toast.makeText(requireContext(), R.string.send_fail, Toast.LENGTH_SHORT)
                            .show()
                    })
            }

        }

        mChatSharedVM.messageLiveData.observe(viewLifecycleOwner,
            Observer<MutableList<Message>?> {
                it?.let {
                    mMsgList.clear()
                    mMsgList.addAll(it)
                    if (mMsgList.size == 0) {
                        mHeaderMsg.isEnd = true
                        mMsgList.add(0, mHeaderMsg)
                    }
                    fetchLink()
                    mMsgListAdapter.notifyDataSetChanged()

                    scrollToBottom()
                }
            })

        //消息更新
        btn_update_message_cancel.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = false)
        }

        //上传文件
        btn_message_menu.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                hideKeyboard()
                emoji_tv.isChecked = false
                mHandler.postDelayed({
                    mBottomSheet.show()
                }, 200)
            } else {
                mBottomSheet.dismiss()
            }
        }

        //表情事件
        emoji_tv.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                hideKeyboard()
                scrollToBottom()
//                section_header_layout.visibility = View.VISIBLE
//                bottom_emoji_rr.visibility = View.VISIBLE
//                loadEmoji()
                reaction_stamp_viewpager.visibility = View.VISIBLE
                btn_message_menu.isChecked = false
            } else {
                reaction_stamp_viewpager.visibility = View.GONE
//                section_header_layout.visibility =
//                    View.GONE
//                bottom_emoji_rr.visibility = View.GONE
            }
        }
        //接受新的Message
        mChatSharedVM.messageCreateLD.observe(viewLifecycleOwner, Observer { event ->

            if (event.message.channelId == mChannelId) {
                val msg = mMsgList.find { msgInList ->
                    event.message.nonce == msgInList.nonce && event.message.authorId == msgInList.authorId
                }
                if (msg == null) {//接收新的msg
                    mMsgList.add(event.message)
                    event.message.content?.let {
                        if (Assets.regexLink.containsMatchIn(it))
                            fetchLink()
                    }
                    mMsgListAdapter.notifyDataSetChanged()
                } else {//发送附件，删除刚发送的本地msg
                    val index = mMsgList.indexOf(msg)
                    mMsgList[index] = event.message
                    mMsgListAdapter.notifyItemChanged(index)
                }
            }
        })

        //show user profile
        mChatSharedVM.showUserProfileLD.observe(viewLifecycleOwner, Observer {
            val guildUserInfoFragment = GuildUserInfoFragment(it.id)
            guildUserInfoFragment.show(
                parentFragmentManager,
                guildUserInfoFragment.tag
            )
        })

        //删除消息
        mChatSharedVM.messageDeleteLD.observe(viewLifecycleOwner, Observer {
            if (it.message.channelId == mChannelId) {
                var removeIndex = -1
                for ((index, value) in mMsgList.withIndex()) {
                    if (value.id == it.message.id) {
                        removeIndex = index
                        break
                    }
                }
                if (removeIndex > -1) {
                    mMsgList.removeAt(removeIndex)
                    mMsgListAdapter.notifyItemRemoved(removeIndex)
                }
                if (removeIndex == 1) {
                    view_messages.post(Runnable {
                        view_messages.scrollToPosition(0)
                    })
                }
            }
        })

        mChatSharedVM.messageUpdateLD.observe(viewLifecycleOwner, Observer { event ->
            val msg = mMsgList.find {
                it.id == event.message.id
            }
            msg?.let {
                val index = mMsgList.indexOf(it)
                mMsgList[index] = event.message
                mMsgListAdapter.notifyItemChanged(index)
            }
        })

        //Reaction add
        mChatSharedVM.reactionAddLD.observe(viewLifecycleOwner, Observer {
            if (it.reaction.message?.channelId == mChannelId) {
                var indexToReplace = 0
                val newReac = it.reaction
                for ((index, value) in mMsgList.withIndex()) {
                    newReac.message?.let {
                        if (it.id == value.id) {
                            indexToReplace = index
                            value.reactions.put(newReac.id, newReac)
                        }
                    }
                }
                mMsgListAdapter.notifyItemChanged(indexToReplace)
            }
        })


        //Reaction remove
        mChatSharedVM.reactionRemoveLD.observe(viewLifecycleOwner, Observer {
            if (it.reaction.message?.channelId == mChannelId) {
                var indexToReplace = 0
                val removeReac = it.reaction
                for ((index, value) in mMsgList.withIndex()) {
                    removeReac.message?.let {
                        if (it.id == value.id) {
                            indexToReplace = index
                            val updateReaction = value.reactions[removeReac.id]
                            updateReaction?.let {
                                if (it.count == 0) {
                                    value.reactions.remove(removeReac.id)
                                } else {
                                    value.reactions[removeReac.id] = it
                                }
                            }
                        }
                    }
                }
                mMsgListAdapter.notifyItemChanged(indexToReplace)
            }
        })

        //加载更老的消息
        mChatSharedVM.messageMoreLiveData.observe(viewLifecycleOwner, Observer {
            mMsgList.removeAt(0)
            mMsgListAdapter.notifyItemRemoved(0)

            if (it.size == 0) {
                mHeaderMsg.isEnd = true
                mMsgList.add(0, mHeaderMsg)
                mMsgListAdapter.notifyItemInserted(0)
                isFetchingMore = false
                return@Observer
            } else {
                mMsgList.addAll(0, it)
                mMsgListAdapter.notifyItemRangeInserted(0, it.size)
                isFetchingMore = false
                mHeaderMsg.isEnd = false
            }
        })
        //setup recycler view
        mLayoutManager = LinearLayoutManager(requireContext())
        mLayoutManager.stackFromEnd = true
        view_messages.layoutManager = mLayoutManager
        view_messages.adapter = mMsgListAdapter
        OverScrollDecoratorHelper.setUpOverScroll(
            view_messages,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )
        val animOut = TranslateAnimation(0f, 0f, 0f, 450f)
        animOut.setInterpolator(LinearInterpolator())
        animOut.duration = 400
        val animIn = TranslateAnimation(0f, 0f, 450f, 0f)
        animIn.setInterpolator(LinearInterpolator())
        animIn.duration = 400


        btn_scroll_to_bottom.setOnClickListener {
            view_messages.scrollToPosition(mMsgListAdapter.itemCount - 1)
            btn_scroll_to_bottom.startAnimation(animOut)
            btn_scroll_to_bottom.visibility = View.GONE
        }

        view_messages.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    btn_message_menu.isChecked = false
                    emoji_tv.isChecked = false
                    hideKeyboard()
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && mLayoutManager.findFirstVisibleItemPosition() == 0) {
                    fetchMore()
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mLayoutManager.findLastVisibleItemPosition() < mMsgListAdapter.itemCount - 16) {
                    if (btn_scroll_to_bottom.visibility == View.GONE) {
                        btn_scroll_to_bottom.visibility = View.VISIBLE
                        btn_scroll_to_bottom.startAnimation(animIn)
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE && mLayoutManager.findLastVisibleItemPosition() >= mMsgListAdapter.itemCount - 16) {
                    if (btn_scroll_to_bottom.visibility == View.VISIBLE) {
                        btn_scroll_to_bottom.startAnimation(animOut)
                        btn_scroll_to_bottom.visibility = View.GONE
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    if (mChannelId != null) {
                        if (Client.global.channels[mChannelId!!] is TextChannel) {
                            (Client.global.channels[mChannelId!!] as TextChannel).apply {
                                if (messages.list.size != 0) {
                                    val lastMessageId = messages.list[messages.size - 1]
                                    val lastMessage = messages[lastMessageId.substring(2)]
                                    patch(JsonObject().apply {
                                        addProperty("ack_message_id", lastMessageId)
                                        addProperty("last_message_id", lastMessageId)
                                    })
                                    this.mention = 0
                                    lastMessage?.let {
                                        it.ack().observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                client.eventBus.postEvent(MessageReadEvent(message = lastMessage))
                                            }, {
                                                Logger.d(it.message)
                                            })
                                    }
                                }
                            }
                        }
                        if (Client.global.channels[mChannelId!!] is DmChannel) {
                            (Client.global.channels[mChannelId!!] as DmChannel).apply {
                                if (messages.list.size != 0) {
                                    val lastMessageId = messages.list[messages.size - 1]
                                    val lastMessage = messages[lastMessageId.substring(2)]
                                    patch(JsonObject().apply {
                                        addProperty("ack_message_id", lastMessageId)
                                    })
                                    lastMessage?.let {
                                        it.ack().observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                val map = mChatSharedVM.dmUnReadLiveData.value
                                                mChannelId?.let { cId ->
                                                    map?.put(cId, 0)
                                                    map?.let { updatedMap ->
                                                        mChatSharedVM.dmUnReadLiveData.value =
                                                            updatedMap
                                                    }
                                                }
                                            }, { error ->
                                                Logger.d(error.message)
                                            })
                                    }
                                }
                            }
                        }
                    }

                }
                //load more message when user scroll up
                if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
                    fetchMore()
                }
            }
        }
        )
        setUpBottomSheet()

        mMsgListAdapter.onItemClickListner = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                hideKeyboard()
            }
        }

        mChatSharedVM.botCommandSelectedLD.observe(viewLifecycleOwner, Observer {
            editText.append(it)
            btn_message_send.performClick()
        })
    }


    private fun scrollToBottom() {
        mLayoutManager.scrollToPosition(mMsgList.size - 1)
    }

    private fun fetchMore() {
        if (!isFetchingMore) {
            isFetchingMore = true
            if (!mHeaderMsg.isEnd) {
                if (mMsgList.size != 0) {
                    if (mMsgList[0] !is HeaderMessage) {
                        mMsgList.add(0, mHeaderMsg)
                        mMsgListAdapter.notifyItemInserted(0)
                    }
                }

                mHandler.postDelayed({
                    mChannelId?.let { cId ->
                        if (mMsgList.size > 1) {
                            mMsgList[1].id?.let { mId ->
                                mChatSharedVM.loadOldMessage(cId, mId)
                            }
                        }
                    }
                }, 1000)
            } else {
                isFetchingMore = false
                if (mMsgList[0] !is HeaderMessage) {
                    mMsgList.add(0, mHeaderMsg)
                    mMsgListAdapter.notifyItemInserted(0)
                }
            }
        }
    }

    private fun setUpBottomSheet() {
        mBottomSheet =
            FileBottomSheetFragment(
                requireActivity(),
                onBottomSheetSelect = object : OnBottomSheetSelect {
                    override fun onItemSelected(index: Int) {

                        when (index) {
                            0 -> {
                                pickFile(IMAGE_REQUEST_CODE)
                            }
                            1 -> {
                                Dexter.withContext(requireContext())
                                    .withPermission(
                                        Manifest.permission.CAMERA
                                    )
                                    .withListener(object : PermissionListener {
                                        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                                            mEasyImage.openCameraForImage(this@ChannelPanelFragment)
                                        }

                                        override fun onPermissionRationaleShouldBeShown(
                                            p0: PermissionRequest?,
                                            p1: PermissionToken?
                                        ) {

                                        }

                                        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                                        }
                                    }).check()

                            }
                            2 -> {
                                pickFile(FILE_REQUEST_CODE)
                            }
                        }
                        mBottomSheet.dismiss(true)

                    }
                })

        mBottomSheet.setOnDismissListener {
            btn_message_menu.isChecked = false
        }
    }

    fun pickFile(code: Int) {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        // Filter to only show results that can be "opened", such as files
                        addCategory(Intent.CATEGORY_OPENABLE)
                        // search for all documents available via installed storage providers
                        type = if (code == FILE_REQUEST_CODE) "*/*" else "image/*"
                    }
                    startActivityForResult(intent, code)
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                }
            }).check()
    }


    fun getNonce(): Long {
        return SnowFlakesGenerator(
            Random(System.currentTimeMillis()).nextInt(SnowFlakesGenerator.MAX_MACHINE_ID)
        ).nextId()
    }


    fun createEmptyMessageWithAttachment(
        requestCode: Int,
        cacheFile: File,
        size: Long
    ): Message {
        val msgObject = JsonObject()
        msgObject.addProperty("id", "")
        msgObject.addProperty("nonce", getNonce())
        msgObject.addProperty("channel_id", mChannelId)
        msgObject.addProperty(
            "timestamp",
            LocalDateTime.now().minusHours(8).toString()
        )
        msgObject.addProperty("authorId", Client.global.me.id)
        val userObject = JsonObject()
        userObject.addProperty("id", Client.global.me.id)
        userObject.addProperty("username", Client.global.me.username)
        userObject.addProperty(
            "discriminator",
            Client.global.me.discriminator
        )
        userObject.addProperty("name", Client.global.me.name)
        userObject.addProperty("avatar", Client.global.me.avatar)
        userObject.addProperty("avatar_url", Client.global.me.avatarURL)
        msgObject.add("author", userObject)
        val msg = Message(client = Client.global, data = msgObject)
        val attachmentObj = JsonObject()
        attachmentObj.addProperty("id", "new_image")
        attachmentObj.addProperty("filename", cacheFile.absolutePath)
        if (requestCode == IMAGE_REQUEST_CODE) {
            attachmentObj.addProperty(
                "type",
                cacheFile.absolutePath.substringAfterLast(".", "")
            )
            val op = BitmapFactory.Options()
            op.inJustDecodeBounds = true
            val bitmap = BitmapFactory.decodeStream(
                FileInputStream(cacheFile), null, op
            )
            attachmentObj.addProperty("width", bitmap?.height)
            attachmentObj.addProperty("height", bitmap?.width)
            bitmap?.recycle()
        } else {
            attachmentObj.addProperty(
                "type",
                cacheFile.absolutePath.substringAfterLast(".", "")
            )
        }
        attachmentObj.addProperty("size", size.toInt())
        val attachment = MessageAttachment(Client.global, attachmentObj)
        msg.attachments["new_attachment"] = attachment
        msg.isSending = true
        return msg
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == FILE_REQUEST_CODE || requestCode == IMAGE_REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
            data?.let {
                it.data?.let { uri ->

                    var size = 0L
                    var name = ""
                    requireContext().contentResolver.query(uri, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        cursor.moveToFirst()
                        size = cursor.getLong(sizeIndex)
                        name = cursor.getString(nameIndex)
                        cursor.close()
                    }

                    if (size > 8000000) {
                        Toast.makeText(requireContext(), "附件大小不能超过8MB", Toast.LENGTH_SHORT).show()
                        return@let
                    }

                    val cacheFile = File(requireContext().cacheDir, name)

                    val outputStream = FileOutputStream(cacheFile)

                    IOUtils.copy(
                        requireContext().contentResolver.openInputStream(uri),
                        outputStream
                    )

                    val msgObject = JsonObject()
                    msgObject.addProperty("id", "")
                    msgObject.addProperty("nonce", getNonce())
                    msgObject.addProperty("channel_id", mChannelId)
                    msgObject.addProperty(
                        "timestamp",
                        LocalDateTime.now().minusHours(8).toString()
                    )
                    msgObject.addProperty("authorId", Client.global.me.id)
                    val userObject = JsonObject()
                    userObject.addProperty("id", Client.global.me.id)
                    userObject.addProperty("username", Client.global.me.username)
                    userObject.addProperty(
                        "discriminator",
                        Client.global.me.discriminator
                    )
                    userObject.addProperty("name", Client.global.me.name)
                    userObject.addProperty("avatar", Client.global.me.avatar)
                    userObject.addProperty("avatar_url", Client.global.me.avatarURL)
                    msgObject.add("author", userObject)
                    val msg = Message(client = Client.global, data = msgObject)
                    val attachmentObj = JsonObject()
                    attachmentObj.addProperty("id", "new_image")
                    attachmentObj.addProperty("filename", cacheFile.absolutePath)
                    if (requestCode == IMAGE_REQUEST_CODE) {
                        attachmentObj.addProperty(
                            "type",
                            cacheFile.absolutePath.substringAfterLast(".", "")
                        )
                        val op = BitmapFactory.Options()
                        op.inJustDecodeBounds = true
                        val bitmap = BitmapFactory.decodeStream(
                            requireContext().contentResolver.openInputStream(uri), null, op
                        )
                        attachmentObj.addProperty("width", bitmap?.height)
                        attachmentObj.addProperty("height", bitmap?.width)
                        bitmap?.recycle()
                    } else {
                        attachmentObj.addProperty(
                            "type",
                            cacheFile.absolutePath.substringAfterLast(".", "")
                        )
                    }
                    attachmentObj.addProperty("size", size.toInt())
                    val attachment = MessageAttachment(Client.global, attachmentObj)
                    msg.attachments["new_attachment"] = attachment
                    msg.isSending = true

                    mMsgList.add(msg)
                    mMsgListAdapter.notifyItemInserted(mMsgList.size - 1)
                    scrollToBottom()
                    uploadFile(cacheFile, msg)
                }
            }
        }

        mEasyImage.handleActivityResult(
            requestCode,
            resultCode,
            data,
            requireActivity(),
            object : EasyImage.Callbacks {
                override fun onCanceled(source: MediaSource) {

                }

                override fun onImagePickerError(error: Throwable, source: MediaSource) {

                }

                override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                    val newMsg = createEmptyMessageWithAttachment(
                        IMAGE_REQUEST_CODE,
                        imageFiles[0].file,
                        imageFiles[0].file.length()
                    )

                    mMsgList.add(newMsg)
                    mMsgListAdapter.notifyItemInserted(mMsgList.size - 1)
                    scrollToBottom()
                    uploadFile(imageFiles[0].file, newMsg)
                }
            })
    }

    private fun mentionFormat(identifier: String): String {
        return "@${identifier}"
    }

    private fun callMentionBottomSheet(mentionList: MutableList<GuildMember>) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.botom_sheet_mention, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        val mentionAdapter =
            MentionListAdapter(mentionList, object : MentionListAdapter.OnMentionSelectedListener {
                override fun onMentionSelected(identifier: String) {
                    mChatSharedVM.mentionState.value?.let {
                        editText.text.replace(
                            it.start, it.start + 1,
                            mentionFormat(identifier)
                        )
                        editText.text.setSpan(
                            ForegroundColorSpan(requireContext().getColor(R.color.secondary)),
                            it.start,
                            it.start + mentionFormat(identifier).length,
                            0x11
                        )
                    }
                    dialog.dismiss()
                }
            })
        val mentionPanel = view.findViewById<RecyclerView>(R.id.mention_panel)
        mentionPanel.adapter = mentionAdapter
        mentionPanel.layoutManager = LinearLayoutManager(context)
        mentionAdapter.notifyDataSetChanged()
        dialog.window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            ?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        dialog.show()
    }

    private fun uploadFile(file: File, msg: Message) {

        MultipartUploadRequest(
            requireContext(),
            serverUrl = NetworkConfigs.baseUrl + "channels/${msg.channelId}/messages"
        )
            .setMethod("POST")
            .setUploadID(msg.nonce!!)
            .setNotificationConfig { _: Context, _: String ->
                UploadNotificationConfig(
                    notificationChannelId = "1",
                    isRingToneEnabled = false,
                    progress = UploadNotificationStatusConfig(
                        title = "上传中",
                        message = "请稍候",
                        iconColorResourceID = R.color.background4,
                        iconResourceID = R.drawable.app_logo
                    ),
                    success = UploadNotificationStatusConfig(
                        title = "上传成功",
                        message = "恭喜上传成功啦!",
                        iconColorResourceID = R.color.background4,
                        iconResourceID = R.drawable.app_logo
                    ),
                    error = UploadNotificationStatusConfig(
                        title = "上传失败",
                        message = "上传失败,嘤嘤嘤！",
                        iconColorResourceID = R.color.background4,
                        iconResourceID = R.drawable.app_logo
                    ),
                    cancelled = UploadNotificationStatusConfig(
                        title = "上传取消",
                        message = "上传已取消,嘤嘤嘤!",
                        iconColorResourceID = R.color.background4,
                        iconResourceID = R.drawable.app_logo
                    )
                )
            }
            .addParameter("payload_json", "{\"content\":null,\"nonce\":\"${msg.nonce}\"}")
            .addHeader("Authorization", Client.global.auth)
            .addFileToUpload(file.absolutePath, parameterName = file.name, fileName = file.name)
            .subscribe(requireContext(), viewLifecycleOwner, object : RequestObserverDelegate {
                override fun onCompleted(context: Context, uploadInfo: UploadInfo) {

                }

                override fun onCompletedWhileNotObserving() {

                }

                override fun onError(
                    context: Context,
                    uploadInfo: UploadInfo,
                    exception: Throwable
                ) {
                    val deletedMsg = mMsgList.find {
                        it.nonce == uploadInfo.uploadId
                    }
                    deletedMsg?.let {
                        val index = mMsgList.indexOf(it)
                        mMsgList.remove(it)
                        mMsgListAdapter.notifyItemRemoved(index)
                        Toast.makeText(requireContext(), R.string.send_fail, Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onProgress(context: Context, uploadInfo: UploadInfo) {

                }

                override fun onSuccess(
                    context: Context,
                    uploadInfo: UploadInfo,
                    serverResponse: ServerResponse
                ) {

                }
            })
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = requireActivity().currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun createEmptyMsg(content: String?): Message {
        val msgObject = JsonObject()
        msgObject.addProperty("id", "")
        msgObject.addProperty("nonce", getNonce())
        msgObject.addProperty("channel_id", mChannelId)
        msgObject.addProperty("timestamp", LocalDateTime.now().minusHours(8).toString())
        msgObject.addProperty("authorId", Client.global.me.id)
        msgObject.addProperty("content", content)
        val userObject = JsonObject()
        userObject.addProperty("id", Client.global.me.id)
        userObject.addProperty("username", Client.global.me.username)
        userObject.addProperty("discriminator", Client.global.me.discriminator)
        userObject.addProperty("name", Client.global.me.name)
        userObject.addProperty("avatar", Client.global.me.avatar)
        userObject.addProperty("avatar_url", Client.global.me.avatarURL)
        msgObject.add("author", userObject)

        val msg = Message(client = Client.global, data = msgObject)
        msg.isSending = true
        return msg
    }

    fun fetchLink() {
        mMsgList.forEachIndexed { index, mes ->
            mes.content?.let { content ->
                if (Assets.regexLink.containsMatchIn(content)) {
                    val links = Assets.linkParser(content)
                    val disposable =
                        Observable.create(ObservableOnSubscribe<MutableList<Link>> { emitter ->
                            val linkList = mutableListOf<Link>()
                            linkList.clear()
                            try {
                                links?.let {
                                    it.forEach { link ->
                                        var imgStr = ""
                                        val document: Document =
                                            Jsoup.parse(
                                                URL(link.url.trim()),
                                                5000
                                            )
                                        val title: String =
                                            document.head().getElementsByTag("title").text()
                                        val imgs: Elements = document.getElementsByTag("img")
                                        if (imgs.size > 0) {
                                            imgStr = imgs.get(0).attr("abs:src")
                                        }
                                        val link = Link(
                                            title = title,
                                            url = link.url.trim(),
                                            code = 1,
                                            img = imgStr,
                                            messageId = mes?.id.toString(),
                                            position = index,
                                            content = null
                                        )
                                        linkList.add(link)
                                    }
                                }
                                emitter.onNext(linkList)
                            } catch (e: IOException) {
                                linkList.clear()
                                emitter.onNext(linkList)
                                e.printStackTrace()
                            }
                        }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { linkList ->
                                Client.global.eventBus.postEvent(LinkParseReadyEvent(linkList = linkList))
                            }
                }

            }

        }
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? =
                requireContext().contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result =
                        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

}

class ViewPagerCollectionAdapter(
    val onEmojiClickListener: OnEmojiClickListener,
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = EmojiFragment(onEmojiClickListener = onEmojiClickListener)
                return fragment
            }
            1 -> {
                val fragment = StampFragment()
                return fragment
            }
            else -> Fragment()
        }

    }

    override fun getCount(): Int {
        return 2
    }

}


fun ContentResolver.getFileName(fileUri: Uri): String {

    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }

    return name
}