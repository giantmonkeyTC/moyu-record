package cn.troph.tomon.ui.chat.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.*
import android.os.FileUtils.copy
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
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
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.utils.DensityUtil
import cn.troph.tomon.core.utils.SnowFlakesGenerator
import cn.troph.tomon.core.utils.Url
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.emoji.*
import cn.troph.tomon.ui.chat.messages.INVITE_LINK
import cn.troph.tomon.ui.chat.messages.MessageAdapter
import cn.troph.tomon.ui.chat.messages.MessageViewModel
import cn.troph.tomon.ui.chat.messages.ReactionSelectorListener
import cn.troph.tomon.ui.chat.ui.SpacesItemDecoration
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
import com.alibaba.sdk.android.oss.common.utils.IOUtils
import com.arthurivanets.bottomsheets.BottomSheet
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.notifyAll
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

const val FILE_REQUEST_CODE_FILE = 323

class ChannelPanelFragment : Fragment() {

    private lateinit var mBottomEmojiAdapter: BottomEmojiAdapter
    private lateinit var mSectionDataManager: SectionDataManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    private val msgViewModel: MessageViewModel by viewModels()
    private lateinit var mBottomSheet: FileBottomSheetFragment
    private lateinit var mLayoutManager: LinearLayoutManager
    private val mHandler = Handler()

    private val mEmojiClickListener = object : OnEmojiClickListener {
        override fun onEmojiSelected(emojiCode: String) {
            editText.text?.append(emojiCode)
            editText.clearFocus()
        }

        override fun onSystemEmojiSelected(unicode: Int) {
            editText.text?.append(String(Character.toChars(unicode)))
            editText.clearFocus()
        }
    }

    private var channelId: String? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed && value != null) {
                val channel = Client.global.channels[value]
                if (channel is DmChannel) {
                    msgViewModel.loadDmChannelMessage(value)
                } else if (channel is TextChannel) {
                    val count = mMsgList.size
                    mMsgList.clear()
                    msgListAdapter.notifyItemRangeRemoved(0, count)
                    msgViewModel.loadTextChannelMessage(value)
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
        AppState.global.updateEnabled.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                message = it.message
                updateEnabled = it.flag
            }
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                channelId = it.channelId
            }
        return inflater.inflate(R.layout.fragment_channel_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        msgViewModel.messageLoadingLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                loading_text_view.visibility = View.VISIBLE
                loading_text_view.playAnimation()
            } else {
                loading_text_view.visibility = View.GONE
                loading_text_view.cancelAnimation()
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
        var longLastClickTime = 0L
        btn_message_send.setOnClickListener {
            if (SystemClock.elapsedRealtime() - longLastClickTime < 1000) {
                return@setOnClickListener
            }
            longLastClickTime = SystemClock.elapsedRealtime()
            if (channelId == null) {
                return@setOnClickListener
            }
            val textToSend = editText.text.toString()
            editText.text = null
            if (!AppState.global.updateEnabled.value.flag) {
                (Client.global.channels[channelId
                    ?: ""] as TextChannelBase).messages.create(textToSend)
                    .observeOn(AndroidSchedulers.mainThread()).doOnError { error -> println(error) }
                    .subscribe {
                        mLayoutManager.scrollToPosition(mMsgList.size - 1)
                    }
            } else {
                message!!.update(textToSend)
                    .observeOn(AndroidSchedulers.mainThread()).doOnError { error ->
                        println(
                            error
                        )
                    }.subscribe {
                        AppState.global.updateEnabled.value =
                            UpdateEnabled(flag = false)
                        for ((index, value) in mMsgList.withIndex()) {
                            if (value.id == it.id) {
                                msgListAdapter.notifyItemChanged(index)
                                break
                            }
                        }
                        mLayoutManager.scrollToPosition(mMsgList.size - 1)
                    }
            }

        }
        mLayoutManager = LinearLayoutManager(requireContext())
        mLayoutManager.stackFromEnd = true
        view_messages.layoutManager = mLayoutManager
        view_messages.addItemDecoration(
            SpacesItemDecoration(
                DensityUtil.dip2px(
                    requireContext(),
                    5f
                )
            )
        )
        view_messages.adapter = msgListAdapter
        msgViewModel.getMessageLiveData().observe(viewLifecycleOwner,
            Observer<MutableList<Message>?> {
                it?.let {
                    mMsgList.clear()
                    mMsgList.addAll(it)
                    msgListAdapter.notifyDataSetChanged()
                }
            })

        //加载更多消息
        swipe_refresh_ll.setProgressViewEndTarget(false, 0)
        swipe_refresh_ll.setOnRefreshListener {
            mMsgList.add(0, mHeaderMsg)
            msgListAdapter.notifyItemInserted(0)
            mHandler.postDelayed(Runnable {
                channelId?.let {
                    val cId = it
                    if (mMsgList.size >= 1) {
                        mMsgList[1].id?.let {
                            msgViewModel.loadOldMessage(cId, it)
                        }
                    }
                }
            }, 1000)
        }

        //消息更新
        btn_update_message_cancel.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = false)
        }

        //选择文件事件
        btn_message_menu.setOnClickListener {
            hideKeyboard(requireActivity())
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
                bottom_emoji_rr.visibility = View.GONE
                activity?.let {
                    hideKeyboard(it)
                }
                loadEmoji()
            }
        }
        //接受新的Message
        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>().subscribe(Consumer {
            if (it.message.channelId == channelId) {
                mMsgList.add(it.message)
                msgListAdapter.notifyItemInserted(mMsgList.size - 1)
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
                            value.reactions.remove(removeReac.id)
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
            swipe_refresh_ll.isRefreshing = false
            if (it.size == 0) {
                val toast = Toast.makeText(requireContext(), "没有更多消息了 :(", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP, 0, 200)
                toast.show()
                return@Observer
            } else {
                mMsgList.addAll(0, it)
                msgListAdapter.notifyItemRangeInserted(0, it.size)
            }
        })
    }

    private fun loadEmoji() {
        val guildIcon = mutableListOf<String>()
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

        for (item in Client.global.guilds.list) {
            if (item.emojis.values.toMutableList().size == 0)
                continue
            val sectionData = CustomGuildEmoji(
                item.id,
                name = item.name,
                isBuildIn = false,
                emojiList = item.emojis.values.toMutableList()
            )
            item.iconURL?.let {
                guildIcon.add(it)
            }

            val sectionAdapter = EmojiAdapter(sectionData, mEmojiClickListener)
            mSectionDataManager.addSection(sectionAdapter, 1)
        }
        val systemEmoji = SystemEmoji()
        mSectionDataManager.addSection(
            EmojiAdapter(
                systemEmoji.getSystemEmojiEmoticons(),
                mEmojiClickListener
            ), 1
        )
        mSectionDataManager.addSection(
            EmojiAdapter(
                systemEmoji.getSystemEmojiDingbats(),
                mEmojiClickListener
            ), 1
        )
        mSectionDataManager.addSection(
            EmojiAdapter(
                systemEmoji.getSystemEmojiTransport(),
                mEmojiClickListener
            ), 1
        )

        emoji_rr.adapter = mSectionDataManager.adapter
        section_header_layout.attachTo(emoji_rr, mSectionDataManager)

        mBottomEmojiAdapter = BottomEmojiAdapter(
            guildIcon,
            onBottomGuildSelectedListener = object : OnBottomGuildSelectedListener {
                override fun onGuildSelected(position: Int) {
                }
            })
        bottom_emoji_rr.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        bottom_emoji_rr.adapter = mBottomEmojiAdapter

    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun uploadFile(file: File) {
        val requestFile = file.asRequestBody()
        val requestBody =
            "{\"content\":null,\"nonce\":\"${SnowFlakesGenerator(1).nextId()}\"}".toRequestBody()
        val map = mutableMapOf<String, RequestBody>()
        map["payload_json"] = requestBody
        val body = MultipartBody.Part.createFormData(file.name, file.name, requestFile)
        (Client.global.channels[channelId!!] as TextChannelBase).messages.uploadAttachments(
            partMap = map,
            files = body
        ).observeOn(AndroidSchedulers.mainThread()).subscribe {

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_REQUEST_CODE_FILE && resultCode == Activity.RESULT_OK) {
            data?.let {
                it.getParcelableArrayListExtra<MediaFile>(FilePickerActivity.MEDIA_FILES)?.let {
                    for (item in it) {
                        Logger.d(item.uri)
                        val parcelFileDescriptor =
                            requireContext().contentResolver.openFileDescriptor(item.uri, "r", null)

                        parcelFileDescriptor?.let {
                            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                            val file =
                                File(
                                    requireContext().cacheDir,
                                    requireContext().contentResolver.getFileName(item.uri)
                                )
                            val outputStream = FileOutputStream(file)
                            org.apache.commons.io.IOUtils.copy(inputStream, outputStream)
                            uploadFile(file)
                        }
                    }
                }
            }
        }
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