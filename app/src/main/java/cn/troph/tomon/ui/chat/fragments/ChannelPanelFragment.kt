package cn.troph.tomon.ui.chat.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.events.MessageUpdateEvent
import cn.troph.tomon.core.events.ReactionAddEvent
import cn.troph.tomon.core.events.ReactionRemoveEvent
import cn.troph.tomon.core.structures.HeaderMessage
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.core.utils.SnowFlakesGenerator
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.emoji.*
import cn.troph.tomon.ui.chat.messages.MessageAdapter
import cn.troph.tomon.ui.chat.messages.MessageViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
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
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.notifyAll
import java.io.File

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
                if (channel is TextChannelBase) {
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
    private val msgListAdapter: MessageAdapter = MessageAdapter(mMsgList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiCompat.init(BundledEmojiCompatConfig(requireContext()))
    }

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
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (section_header_layout.isVisible) {
                    section_header_layout.visibility =
                        View.GONE
                    bottom_emoji_rr.visibility = View.GONE
                }
            }
        }
        btn_message_send.setOnClickListener {
            if (channelId == null) {
                return@setOnClickListener
            }
            if (!AppState.global.updateEnabled.value.flag)
                (Client.global.channels[channelId
                    ?: ""] as TextChannel).messages.create(editText.text.toString())
                    .observeOn(AndroidSchedulers.mainThread()).doOnError { error -> println(error) }
                    .subscribe {
                        editText.text = null
                    }
            else
                message!!.update(editText.text.toString())
                    .observeOn(AndroidSchedulers.mainThread()).doOnError { error ->
                        println(
                            error
                        )
                    }.subscribe {
                        AppState.global.updateEnabled.value =
                            UpdateEnabled(flag = false)
                        editText.text = null
                    }
        }
        mLayoutManager = LinearLayoutManager(requireContext())
        mLayoutManager.stackFromEnd = true
        view_messages.layoutManager = mLayoutManager
        view_messages.adapter = msgListAdapter
        view_messages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(-1)) {
                    Logger.d("at top")
                }
            }
        })
        msgViewModel.getMessageLiveData().observe(viewLifecycleOwner,
            Observer<MutableList<Message>?> {
                it?.let {
                    mMsgList.clear()
                    mMsgList.addAll(it)
                    msgListAdapter.notifyDataSetChanged()
                }
            })

        //加载更多消息
        swipe_refresh_ll.setDistanceToTriggerSync(1)
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
            mMsgList.add(it.message)
            msgListAdapter.notifyDataSetChanged()
        })
        //Reaction add
        Client.global.eventBus.observeEventOnUi<ReactionAddEvent>().subscribe(Consumer {
            var indexToReplace = 0
            val newReac = it.reaction
            Logger.d("${newReac.id} ${newReac.name} ${it.reaction.identifier} ${it.reaction.isChar}")
            //Logger.d("${newReac.emoji?.id} ${newReac.emoji?.name} ${newReac.emoji?.url}")
            for ((index, value) in mMsgList.withIndex()) {
                newReac.message?.let {
                    if (it.id == value.id) {
                        indexToReplace = index
                        value.reactions.put(newReac.id, newReac)
                    }
                }
            }
            msgListAdapter.notifyItemChanged(indexToReplace)
        })
        //Reaction remove
        Client.global.eventBus.observeEventOnUi<ReactionRemoveEvent>().subscribe(Consumer {
            var indexToReplace = 0
            val removeReac = it.reaction
            for ((index, value) in mMsgList.withIndex()) {
                it.reaction.message?.let {
                    if (it.id == value.id) {
                        indexToReplace = index
                        value.reactions.remove(removeReac.id)
                    }
                }
            }
            it.reaction.message?.let {
                msgListAdapter.notifyItemChanged(indexToReplace)
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
        (Client.global.channels[channelId!!] as TextChannel).messages.uploadAttachments(
            partMap = map,
            files = body
        ).observeOn(AndroidSchedulers.mainThread()).subscribe {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_REQUEST_CODE_FILE && resultCode == Activity.RESULT_OK) {
            data?.let {
                it.getParcelableArrayListExtra<MediaFile>(FilePickerActivity.MEDIA_FILES)?.let {
                    for (item in it) {
                        uploadFile(File(item.path))
                    }
                }
            }
        }
    }
}