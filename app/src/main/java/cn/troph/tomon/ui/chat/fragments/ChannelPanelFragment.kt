package cn.troph.tomon.ui.chat.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.emoji.*
import cn.troph.tomon.ui.chat.messages.MessageAdapter
import cn.troph.tomon.ui.chat.messages.MessageViewModel
import cn.troph.tomon.ui.chat.messages.ReactionSelectorListener
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.NetworkChangeReceiver
import cn.troph.tomon.ui.states.UpdateEnabled
import com.arthurivanets.bottomsheets.BottomSheet
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import com.google.gson.JsonObject
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.fragment_channel_panel.*
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
import java.time.LocalDateTime

const val FILE_REQUEST_CODE_FILE = 323
const val LAST_CHANNEL_ID = "last_channel_id"
const val LAST_GUILD_ID = "last_guild_id"

class ChannelPanelFragment : Fragment() {

    private lateinit var mBottomEmojiAdapter: BottomEmojiAdapter
    private lateinit var mSectionDataManager: SectionDataManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    private val msgViewModel: MessageViewModel by viewModels()
    private lateinit var mBottomSheet: FileBottomSheetFragment
    private lateinit var mLayoutManager: LinearLayoutManager
    private val mHandler = Handler()

    private val intentFilter = IntentFilter()
    private val networkChangeReceiver = NetworkChangeReceiver()

    private val mEmojiClickListener = object : OnEmojiClickListener {
        override fun onEmojiSelected(emojiCode: String) {
            editText.text?.append(emojiCode)
            editText.clearFocus()
        }

        override fun onSystemEmojiSelected(unicode: String) {
            editText.text?.append(unicode)
            editText.clearFocus()
        }
    }

    private var channelId: String? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed && value != null) {
                mHeaderMsg.isEnd = false
                editText.post {
                    editText.text = null
                }
                val channel = Client.global.channels[value]
                if (channel is DmChannel) {
                    val count = mMsgList.size
                    mMsgList.clear()
                    msgListAdapter.notifyItemRangeRemoved(0, count)
                    Client.global.preferences.edit {
                        putString(LAST_CHANNEL_ID, value)
                    }
                    msgViewModel.loadDmChannelMessage(value)
                    editText.hint = getString(R.string.emoji_et_hint)
                    btn_message_send.visibility = View.VISIBLE
                } else if (channel is TextChannel) {
                    Client.global.preferences.edit {
                        putString(LAST_GUILD_ID, (channel as TextChannel).guildId)
                        putString(LAST_CHANNEL_ID, value)
                    }
                    val count = mMsgList.size
                    mMsgList.clear()
                    msgListAdapter.notifyItemRangeRemoved(0, count)
                    msgViewModel.loadTextChannelMessage(value)
                    if (channel.isPrivate) {
                        editText.hint = getString(R.string.ed_msg_hint_no_permisson)
                        btn_message_send.visibility = View.GONE
                    } else {
                        editText.hint = getString(R.string.emoji_et_hint)
                        btn_message_send.visibility = View.VISIBLE
                    }
                }

            }
        }

    private var updateEnabled: Boolean = false
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
    private var isFetchingMore = false
    private val mMsgList = mutableListOf<Message>()
    private val msgListAdapter: MessageAdapter =
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
        requireActivity().unregisterReceiver(networkChangeReceiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppState.global.updateEnabled.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                message = it.message
                updateEnabled = it.flag
            }
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.channelId == null) {
                    channelId = Client.global.preferences.getString(LAST_CHANNEL_ID, null)
                } else {
                    channelId = it.channelId
                }

            }
        msgViewModel.messageLoadingLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                shimmer_view_container.visibility = View.VISIBLE
                shimmer_view_container.startShimmer()
            } else {
                shimmer_view_container.visibility = View.GONE
                shimmer_view_container.stopShimmer()
            }
        })

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (section_header_layout.isVisible) {
                    section_header_layout.visibility =
                        View.GONE
                    bottom_emoji_rr.visibility = View.GONE
                }
            }
        }

        KeyboardVisibilityEvent.setEventListener(requireActivity(),
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (isOpen) {
                        if (section_header_layout.isVisible) {
                            section_header_layout.visibility =
                                View.GONE
                            bottom_emoji_rr.visibility = View.GONE
                        }
                    }
                }
            })
        networkChangeReceiver.setTopView(btn_message_send)
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        requireActivity().registerReceiver(networkChangeReceiver, intentFilter)
        var longLastClickTime = 0L
        btn_message_send.setOnClickListener {
            if (SystemClock.elapsedRealtime() - longLastClickTime < 1000) {
                return@setOnClickListener
            }
            longLastClickTime = SystemClock.elapsedRealtime()
            if (channelId.isNullOrEmpty() || editText.text.isNullOrEmpty()) {
                return@setOnClickListener
            }
            val textToSend = editText.text.toString()
            editText.text = null

            if (!AppState.global.updateEnabled.value.flag) {
                //新建一个空message，并加入到RecyclerView
                val emptyMsg = createEmptyMsg(textToSend)
                mMsgList.add(emptyMsg)
                msgListAdapter.notifyItemInserted(mMsgList.size - 1)
                mLayoutManager.scrollToPosition(msgListAdapter.itemCount - 1)
                //发送消息
                (Client.global.channels[channelId
                    ?: ""] as TextChannelBase).messages.create(
                    textToSend,
                    nonce = emptyMsg.nonce.toString()
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ ->
                        mHandler.postDelayed({
                            mLayoutManager.scrollToPosition(mMsgList.size - 1)
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
                                msgListAdapter.notifyItemChanged(index)
                                break
                            }
                        }
                        mLayoutManager.scrollToPosition(mMsgList.size - 1)
                    }, { _ ->
                        Toast.makeText(requireContext(), R.string.send_fail, Toast.LENGTH_SHORT)
                            .show()
                    })
            }

        }
        mLayoutManager = LinearLayoutManager(requireContext())
        mLayoutManager.stackFromEnd = true
        view_messages.layoutManager = mLayoutManager
        view_messages.adapter = msgListAdapter
        view_messages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    if (channelId != null) {
                        if (Client.global.channels[channelId!!] is TextChannel) {
                            (Client.global.channels[channelId!!] as TextChannel).apply {
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
                        if (Client.global.channels[channelId!!] is DmChannel) {
                            (Client.global.channels[channelId!!] as DmChannel).apply {
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
                if (!view_messages.canScrollVertically(-1)) {
                    Logger.d("header:${mHeaderMsg.isEnd}")
                    if (!isFetchingMore) {
                        isFetchingMore = true
                        if (!mHeaderMsg.isEnd) {
                            mMsgList.add(0, mHeaderMsg)
                            msgListAdapter.notifyItemInserted(0)
                            mHandler.postDelayed({
                                channelId?.let {
                                    val cId = it
                                    if (mMsgList.size > 1) {
                                        mMsgList[1].id?.let {
                                            msgViewModel.loadOldMessage(cId, it)
                                        }
                                    } else {
                                        mMsgList.removeAt(0)
                                        msgListAdapter.notifyItemRemoved(0)
                                        isFetchingMore = false
                                    }
                                }
                            }, 1000)
                        } else {
                            mMsgList.add(0, mHeaderMsg)
                            msgListAdapter.notifyItemInserted(0)
                            mHandler.postDelayed({
                                val index = mMsgList.indexOf(mHeaderMsg)
                                mMsgList.removeAt(index)
                                msgListAdapter.notifyItemRemoved(index)
                                isFetchingMore = false
                            }, 2000)
                        }
                    }
                }
            }
        }
        )

        msgViewModel.getMessageLiveData().observe(viewLifecycleOwner,
            Observer<MutableList<Message>?> {
                it?.let {
                    mMsgList.clear()
                    mMsgList.addAll(it)
                    msgListAdapter.notifyDataSetChanged()
                }
            })

        //消息更新
        btn_update_message_cancel.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = false)
        }

        //选择文件事件
        btn_message_menu.setOnClickListener {
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
                    }).also(BottomSheet::show)
        }

        //表情事件
        emoji_tv.setOnClickListener {
            if (section_header_layout.isVisible) {
                section_header_layout.visibility =
                    View.GONE
                bottom_emoji_rr.visibility = View.GONE
            } else {
                section_header_layout.visibility = View.VISIBLE
                bottom_emoji_rr.visibility = View.VISIBLE
                loadEmoji()
            }
        }
        //接受新的Message
        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>().subscribe(Consumer {
            val event = it
            if (it.message.channelId == channelId) {
                val msg = mMsgList.find {

                    val localMsg = it
                    event.message.nonce == localMsg.nonce && localMsg.id.isNullOrEmpty()
                }
                if (msg == null) {
                    mMsgList.add(it.message)
                    msgListAdapter.notifyItemInserted(mMsgList.size - 1)
                } else {
                    val index = mMsgList.indexOf(msg)
                    mMsgList[index] = event.message
                    msgListAdapter.notifyItemChanged(index)
                }


            }
        })
        //Reaction add
        Client.global.eventBus.observeEventOnUi<ReactionAddEvent>().subscribe(Consumer {
            if (it.reaction.message?.channelId == channelId) {
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
                msgListAdapter.notifyItemChanged(indexToReplace)
            }
        })
        //Reaction remove
        Client.global.eventBus.observeEventOnUi<ReactionRemoveEvent>().subscribe(Consumer {
            if (it.reaction.message?.channelId == channelId) {
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
                msgListAdapter.notifyItemChanged(indexToReplace)
            }
        })

        //delete messsage
        Client.global.eventBus.observeEventOnUi<MessageDeleteEvent>().subscribe(Consumer {
            if (it.message.channelId == channelId) {
                var removeIndex = 0
                for ((index, value) in mMsgList.withIndex()) {
                    if (value.id == it.message.id) {
                        removeIndex = index
                        break
                    }
                }
                mMsgList.removeAt(removeIndex)
                msgListAdapter.notifyItemRemoved(removeIndex)
            }

        })


        //加载更老的消息
        msgViewModel.getMessageMoreLiveData().observe(viewLifecycleOwner, Observer {
            mMsgList.removeAt(0)
            msgListAdapter.notifyItemRemoved(0)
            if (it.size == 0) {
                mHeaderMsg.isEnd = true
                isFetchingMore = false
                return@Observer
            } else {
                mMsgList.addAll(0, it)
                msgListAdapter.notifyItemRangeInserted(0, it.size)
                isFetchingMore = false
            }
        })
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
            guildIcon.add(GuildIcon(item.iconURL, item.name))
            val sectionAdapter = EmojiAdapter(sectionData, mEmojiClickListener)
            mSectionDataManager.addSection(sectionAdapter, 1)
        }
        //loading system emoji
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
            guildIcon.add(GuildIcon(null, item.value[0].code))
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
                                msgObject.addProperty("channelId", channelId)
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
                                msgListAdapter.notifyItemInserted(mMsgList.size - 1)
                                mLayoutManager.scrollToPosition(mMsgList.size - 1)
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
        (Client.global.channels[channelId!!] as TextChannelBase).messages.uploadAttachments(
            partMap = map,
            files = body
        ).observeOn(AndroidSchedulers.mainThread()).subscribe({

        }, {
            val deletedMsg = mMsgList.find {
                it.nonce == msg.nonce
            }
            val index = mMsgList.indexOf(deletedMsg)
            mMsgList.remove(deletedMsg)
            msgListAdapter.notifyItemRemoved(index)
            Toast.makeText(requireContext(), R.string.send_fail, Toast.LENGTH_SHORT).show()
        })
    }

    private fun createEmptyMsg(content: String): Message {
        val msgObject = JsonObject()
        msgObject.addProperty("id", "")
        msgObject.addProperty("nonce", SnowFlakesGenerator(1).nextId())
        msgObject.addProperty("channelId", channelId)
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