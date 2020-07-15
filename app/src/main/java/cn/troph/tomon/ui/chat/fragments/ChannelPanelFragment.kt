package cn.troph.tomon.ui.chat.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.*
import cn.troph.tomon.core.structures.*
import cn.troph.tomon.core.utils.SnowFlakesGenerator
import cn.troph.tomon.ui.chat.emoji.*
import cn.troph.tomon.ui.chat.messages.MessageAdapter
import cn.troph.tomon.ui.chat.messages.MessageViewModel
import cn.troph.tomon.ui.chat.messages.ReactionSelectorListener
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.NetworkChangeReceiver
import cn.troph.tomon.ui.states.UpdateEnabled
import com.arthurivanets.bottomsheets.BottomSheet
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import com.google.android.gms.analytics.HitBuilders
import com.google.gson.JsonObject
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

const val FILE_REQUEST_CODE_FILE = 323
const val LAST_CHANNEL_ID = "last_channel_id"
const val LAST_GUILD_ID = "last_guild_id"

class ChannelPanelFragment : BaseFragment() {

    private lateinit var mBottomEmojiAdapter: BottomEmojiAdapter
    private lateinit var mSectionDataManager: SectionDataManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    private val mMsgViewModel: MessageViewModel by viewModels()
    private val mChatSharedVM: ChatSharedViewModel by activityViewModels()
    private lateinit var mLayoutManager: LinearLayoutManager
    private val mHandler = Handler()
    private lateinit var mBottomSheet: FileBottomSheetFragment
    private val mIntentFilter = IntentFilter()
    private val mNetworkChangeReceiver = NetworkChangeReceiver()

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
                mHeaderMsg.isEnd = false
                isFetchingMore.set(false)
                editText?.let {
                    it.text = null
                }
                val channel = Client.global.channels[value]
                if (channel is DmChannel) {
                    mHeaderMsg.isGuild = false
                    channel.recipient?.let {
                        mHeaderMsg.channelText = it.name
                    }
                    val count = mMsgList.size
                    mMsgList.clear()
                    mMsgListAdapter.notifyItemRangeRemoved(0, count)
                    Client.global.preferences.edit {
                        putString(LAST_CHANNEL_ID, value)
                    }
                    mMsgViewModel.loadDmChannelMessage(value)
                    editText?.let {
                        it.isEnabled = true

                        it.hint = getString(R.string.emoji_et_hint)
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
                    val count = mMsgList.size
                    mMsgList.clear()
                    mMsgListAdapter.notifyItemRangeRemoved(0, count)
                    mMsgViewModel.loadTextChannelMessage(value)
                    if (channel.members[Client.global.me.id]?.roles?.collection?.none {
                            it.permissions.has(Permissions.SEND_MESSAGES)
                        }!! || channel.members[Client.global.me.id]?.roles?.collection?.any {
                            val po = channel.permissionOverwrites[it.id]
                            if (po != null)
                                po.denyPermission(Permissions.SEND_MESSAGES)!!
                            else false
                        }!!) {
                        editText?.let {
                            it.hint = getString(R.string.ed_msg_hint_no_permisson)
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
                        btn_message_send?.let {
                            it.visibility = View.VISIBLE
                        }
                    }
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
    private val mHeaderMsg = HeaderMessage(Client.global, JsonObject())
    private val isFetchingMore = AtomicBoolean(false)
    private val mMsgList = mutableListOf<Message>()
    private val mMsgListAdapter: MessageAdapter =
        MessageAdapter(mMsgList, object : ReactionSelectorListener {
            override fun OnReactionAddClicked(msg: Message) {
                val bs = ReactionFragment()
                bs.show(childFragmentManager, "reaction")
                bs.setMessage(msg)
            }
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_panel, container, false)
    }


    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(mNetworkChangeReceiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMsgViewModel.updateLD.observe(viewLifecycleOwner, Observer {
            message = it.message
            isUpdateEnabled = it.flag
        })

        mChatSharedVM.channelSelectionLD.observe(viewLifecycleOwner, Observer {
            if (it.channelId == null) {
                mChannelId = Client.global.preferences.getString(LAST_CHANNEL_ID, null)
            } else {
                mChannelId = it.channelId
            }
        })

        mMsgViewModel.messageLoadingLiveData.observe(viewLifecycleOwner, Observer {
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
            val textToSend = editText.text.toString()
            editText.text = null

            getTracker().send(
                HitBuilders.EventBuilder().setCategory("TEST").setAction("Send").build()
            )

            if (!AppState.global.updateEnabled.value.flag) {
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

        mMsgViewModel.getMessageLiveData().observe(viewLifecycleOwner,
            Observer<MutableList<Message>?> {
                it?.let {
                    mMsgList.clear()
                    mMsgList.addAll(it)
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
                mBottomSheet.show()
                emoji_tv.isChecked = false
            } else {
                mBottomSheet.dismiss()
            }
        }

        //表情事件
        emoji_tv.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                hideKeyboard()
                scrollToBottom()
                section_header_layout.visibility = View.VISIBLE
                bottom_emoji_rr.visibility = View.VISIBLE
                loadEmoji()
                btn_message_menu.isChecked = false
            } else {
                section_header_layout.visibility =
                    View.GONE
                bottom_emoji_rr.visibility = View.GONE
            }
        }
        //接受新的Message
        mMsgViewModel.messageCreateLD.observe(viewLifecycleOwner, Observer {
            val event = it
            if (it.message.channelId == mChannelId) {
                val msg = mMsgList.find {
                    val localMsg = it
                    event.message.nonce == localMsg.nonce && localMsg.id.isNullOrEmpty()
                }
                if (msg == null) {
                    mMsgList.add(it.message)
                    mMsgListAdapter.notifyItemInserted(mMsgList.size - 1)
                } else {
                    val index = mMsgList.indexOf(msg)
                    mMsgList[index] = event.message
                    mMsgListAdapter.notifyItemChanged(index)
                }
            }
        })

        //删除消息
        mMsgViewModel.messageDeleteLD.observe(viewLifecycleOwner, Observer {
            if (it.message.channelId == mChannelId) {
                var removeIndex = 0
                for ((index, value) in mMsgList.withIndex()) {
                    if (value.id == it.message.id) {
                        removeIndex = index
                        break
                    }
                }
                mMsgList.removeAt(removeIndex)
                mMsgListAdapter.notifyItemRemoved(removeIndex)
            }
        })

        //Reaction add
        mMsgViewModel.reactionAddLD.observe(viewLifecycleOwner, Observer {
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
        mMsgViewModel.reactionRemoveLD.observe(viewLifecycleOwner, Observer {
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
        mMsgViewModel.getMessageMoreLiveData().observe(viewLifecycleOwner, Observer {
            mMsgList.removeAt(0)
            mMsgListAdapter.notifyItemRemoved(0)

            if (it.size == 0) {
                mHeaderMsg.isEnd = true
                mMsgList.add(0, mHeaderMsg)
                mMsgListAdapter.notifyItemInserted(0)
                isFetchingMore.set(false)
                return@Observer
            } else {
                mMsgList.addAll(0, it)
                mMsgListAdapter.notifyItemRangeInserted(0, it.size)
                isFetchingMore.set(false)
            }
        })
        mLayoutManager = LinearLayoutManager(requireContext())
        mLayoutManager.stackFromEnd = true
        view_messages.layoutManager = mLayoutManager
        view_messages.adapter = mMsgListAdapter
        OverScrollDecoratorHelper.setUpOverScroll(
            view_messages,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )
        view_messages.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    btn_message_menu.isChecked = false
                    emoji_tv.isChecked = false
                    hideKeyboard()
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
                                                client.eventBus.postEvent(MessageReadEvent(message = lastMessage))
                                            }, {
                                                Logger.d(it.message)
                                            })
                                    }
                                }
                            }
                        }
                    }

                }
                //load more message when user scroll up
                if (!view_messages.canScrollVertically(-1) && dy < 0) {
                    if (!isFetchingMore.get()) {
                        isFetchingMore.set(true)
                        if (!mHeaderMsg.isEnd) {
                            mMsgList.add(0, mHeaderMsg)
                            mMsgListAdapter.notifyItemInserted(0)
                            mHandler.postDelayed({
                                mChannelId?.let { cId ->
                                    if (mMsgList.size > 1) {
                                        mMsgList[1].id?.let { mId ->
                                            mMsgViewModel.loadOldMessage(cId, mId)
                                        }
                                    }
                                }
                            }, 1000)
                        } else {
                            isFetchingMore.set(false)
                        }
                    }
                }
            }
        }
        )
        setUpBottomSheet()
        mChatSharedVM.setUpChannelSelection()
        mMsgViewModel.setUpEvent()

    }


    private fun scrollToBottom() {
        mLayoutManager.scrollToPosition(mMsgList.size - 1)
    }

    private fun setUpBottomSheet() {
        mBottomSheet =
            FileBottomSheetFragment(
                requireActivity(),
                onBottomSheetSelect = object : OnBottomSheetSelect {
                    override fun onItemSelected(index: Int) {
                        val intent = Intent(requireContext(), FilePickerActivity::class.java)
                        val builder = Configurations.Builder()
                        when (index) {
                            0 -> {
                                builder.setCheckPermission(true).setShowImages(true)
                                    .setShowVideos(false).setShowFiles(false)
                                    .setShowAudios(false)
                                    .setSingleChoiceMode(true).setSingleClickSelection(true)
                                    .enableImageCapture(true)
                                intent.putExtra(
                                    FilePickerActivity.CONFIGS,
                                    builder.build()
                                )
                            }
                            1 -> {
                                builder.setCheckPermission(true).setShowVideos(true)
                                    .setShowImages(false).setShowFiles(false)
                                    .setShowAudios(false)
                                    .setMaxSelection(1).enableVideoCapture(false)
                                intent.putExtra(
                                    FilePickerActivity.CONFIGS,
                                    builder.build()
                                )

                            }
                            2 -> {
                                builder.setCheckPermission(true).setShowFiles(true)
                                    .setSingleChoiceMode(true).setSingleClickSelection(true)
                                    .setShowImages(false).setShowVideos(false)
                                    .setShowAudios(false)
                                intent.putExtra(
                                    FilePickerActivity.CONFIGS,
                                    builder.build()
                                )
                            }
                        }
                        startActivityForResult(intent, FILE_REQUEST_CODE_FILE)
                        mBottomSheet.dismiss(true)

                    }
                })

        mBottomSheet.setOnDismissListener {
            btn_message_menu.isChecked = false
        }
    }

    private fun loadEmoji() {
        val guildIcon = mutableListOf<GuildIcon>()
        mSectionDataManager = SectionDataManager()
        mGridLayoutManager = GridLayoutManager(requireContext(), 7)
        val positionManager: PositionManager = mSectionDataManager
        mGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (positionManager.isHeader(position)) {
                    return mGridLayoutManager.spanCount
                } else {
                    return 1
                }
            }
        }
        emoji_rr.layoutManager = mGridLayoutManager
        //load guild emoji
        for (item in Client.global.guilds.list) {
            if (item.emojis.values.toMutableList().size == 0)
                continue
            val sectionData = CustomGuildEmoji(
                item.id,
                name = item.name,
                isBuildIn = false,
                emojiList = item.emojis.values.toMutableList()
            )
            guildIcon.add(GuildIcon(item.iconURL, item.name, null))
            val sectionAdapter = EmojiAdapter(sectionData, mEmojiClickListener)
            mSectionDataManager.addSection(sectionAdapter, 1)
        }
        //loading system emoji
        val guildIconDefault = mutableListOf<Drawable>()
        guildIconDefault.apply {
            add(resources.getDrawable(R.drawable.ic_running_solid))
            add(resources.getDrawable(R.drawable.ic_smile_solid))
            add(resources.getDrawable(R.drawable.ic_icons_alt_regular))
            add(resources.getDrawable(R.drawable.ic_head_side_solid))
            add(resources.getDrawable(R.drawable.ic_lightbulb_solid))
            add(resources.getDrawable(R.drawable.ic_plane_alt_solid))
            add(resources.getDrawable(R.drawable.ic_flag_solid))
            add(resources.getDrawable(R.drawable.ic_utensils_alt_solid))
            add(resources.getDrawable(R.drawable.ic_leaf_solid))
            add(resources.getDrawable(R.drawable.ic_globe_solid))
        }
        guildIconDefault.forEach {
            guildIcon.add(GuildIcon(null, null, it))
        }
        val systemEmoji = SystemEmoji(requireContext())
        for (item in systemEmoji.returnEmojiWithCategory()) {
            val adapter = EmojiAdapter(
                CustomGuildEmoji(
                    name = item.key,
                    isBuildIn = true,
                    systemEmojiListData = item.value
                ), mEmojiClickListener
            )
            mSectionDataManager.addSection(adapter, 1)
        }

        emoji_rr.adapter = mSectionDataManager.adapter
        section_header_layout.attachTo(emoji_rr, mSectionDataManager)
        // bottom Emoji
        mBottomEmojiAdapter = BottomEmojiAdapter(
            guildIcon,
            onBottomGuildSelectedListener = object : OnBottomGuildSelectedListener {
                override fun onGuildSelected(position: Int) {
                    mGridLayoutManager.scrollToPosition(
                        mSectionDataManager.calcAdapterPos(
                            position,
                            0
                        ) - 1
                    )
                }
            })
        bottom_emoji_rr.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        bottom_emoji_rr.adapter = mBottomEmojiAdapter
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_REQUEST_CODE_FILE && resultCode == Activity.RESULT_OK) {
            data?.let {
                it.getParcelableArrayListExtra<MediaFile>(FilePickerActivity.MEDIA_FILES)
                    ?.let { fileList ->
                        for (item in fileList) {
                            if (item.size > 8 * 1024 * 1024) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.over_size,
                                    Toast.LENGTH_SHORT
                                ).show()
                                break
                            }
                            val parcelFileDescriptor =
                                requireContext().contentResolver.openFileDescriptor(
                                    item.uri,
                                    "r",
                                    null
                                )

                            parcelFileDescriptor?.let {
                                val inputStream =
                                    FileInputStream(parcelFileDescriptor.fileDescriptor)
                                val file =
                                    File(
                                        requireContext().cacheDir,
                                        getFileName(item.uri)
                                    )
                                val outputStream = FileOutputStream(file)
                                IOUtils.copy(inputStream, outputStream)
                                val msgObject = JsonObject()
                                msgObject.addProperty("id", "")
                                msgObject.addProperty("nonce", SnowFlakesGenerator(1).nextId())
                                msgObject.addProperty("channelId", mChannelId)
                                msgObject.addProperty("timestamp", LocalDateTime.now().toString())
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
                                attachmentObj.addProperty("filename", file.absolutePath)
                                if (item.mediaType == MediaFile.TYPE_IMAGE) {
                                    attachmentObj.addProperty(
                                        "type",
                                        file.absolutePath.substringAfterLast(".", "")
                                    )
                                    attachmentObj.addProperty("width", item.width)
                                    attachmentObj.addProperty("height", item.height)
                                } else {
                                    attachmentObj.addProperty(
                                        "type",
                                        file.absolutePath.substringAfterLast(".", "")
                                    )
                                }
                                val attachment = MessageAttachment(Client.global, attachmentObj)
                                msg.attachments["new_attachment"] = attachment
                                msg.isSending = true
                                mMsgList.add(msg)
                                mMsgListAdapter.notifyItemInserted(mMsgList.size - 1)
                                scrollToBottom()
                                uploadFile(file, msg)
                            }
                        }
                    }
            }
        }
    }

    private fun uploadFile(file: File, msg: Message) {
        val requestFile = file.asRequestBody()
        val requestBody =
            "{\"content\":null,\"nonce\":\"${msg.nonce}\"}".toRequestBody()
        val map = mutableMapOf<String, RequestBody>()
        map["payload_json"] = requestBody
        val body = MultipartBody.Part.createFormData(file.name, file.name, requestFile)
        (Client.global.channels[mChannelId!!] as TextChannelBase).messages.uploadAttachments(
            partMap = map,
            files = body
        ).observeOn(AndroidSchedulers.mainThread()).subscribe({

        }, {
            val deletedMsg = mMsgList.find {
                it.nonce == msg.nonce
            }
            val index = mMsgList.indexOf(deletedMsg)
            mMsgList.remove(deletedMsg)
            mMsgListAdapter.notifyItemRemoved(index)
            Toast.makeText(requireContext(), R.string.send_fail, Toast.LENGTH_SHORT).show()
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

    private fun createEmptyMsg(content: String): Message {
        val msgObject = JsonObject()
        msgObject.addProperty("id", "")
        msgObject.addProperty("nonce", SnowFlakesGenerator(1).nextId())
        msgObject.addProperty("channelId", mChannelId)
        msgObject.addProperty("timestamp", LocalDateTime.now().toString())
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

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? =
                requireContext().contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
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