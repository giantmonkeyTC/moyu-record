package cn.troph.tomon.ui.chat.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import cn.troph.tomon.core.collections.EventType
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.core.utils.SnowFlakesGenerator
import cn.troph.tomon.core.utils.event.EventBus
import cn.troph.tomon.core.utils.event.observeEvent
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.emoji.*
import cn.troph.tomon.ui.chat.messages.MessageAdapter
import cn.troph.tomon.ui.chat.messages.MessageViewModel
import cn.troph.tomon.ui.chat.messages.notifyObserver
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
import com.arthurivanets.bottomsheets.BottomSheet
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
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
import java.io.File

const val FILE_REQUEST_CODE_FILE = 323

class ChannelPanelFragment : Fragment() {

    private lateinit var mBottomEmojiAdapter: BottomEmojiAdapter
    private val mSectionDataManager = SectionDataManager()
    private lateinit var mGridLayoutManager: GridLayoutManager
    private val msgViewModel: MessageViewModel by viewModels()
    private lateinit var mBottomSheet: FileBottomSheetFragment
    private val mEmojiClickListener = object : OnEmojiClickListener {
        override fun onEmojiSelected(emojiCode: String) {
            editText.requestFocus()
            editText.text?.append(emojiCode)
        }

        override fun onSystemEmojiSelected(unicode: Int) {
            editText.requestFocus()
            editText.text?.append(String(Character.toChars(unicode)))
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
        view_messages.layoutManager = LinearLayoutManager(requireContext())
        view_messages.adapter = msgListAdapter
        msgViewModel.getMessageLiveData().observe(viewLifecycleOwner,
            Observer<MutableList<Message>?> {
                it?.let {
                    mMsgList.addAll(it)
                    msgListAdapter.notifyDataSetChanged()
                    view_messages.scrollToPosition(msgListAdapter.itemCount - 1)
                }
            })
        view_messages.addOnScrollListener(MessageListOnScrollListener())
        btn_update_message_cancel.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = false)
        }
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

        emoji_tv.setOnClickListener {
            if (section_header_layout.isVisible) {
                section_header_layout.visibility =
                    View.GONE
                bottom_emoji_rr.visibility = View.GONE
                editText.requestFocus()
            } else {
                section_header_layout.visibility = View.VISIBLE
                bottom_emoji_rr.visibility = View.GONE
                activity?.let {
                    hideKeyboard(it)
                }
                loadEmoji()
            }
        }
        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>().subscribe(Consumer {
            mMsgList.add(it.message)
            msgListAdapter.notifyDataSetChanged()
        })
    }

    private fun loadEmoji() {
        val guildIcon = mutableListOf<String>()
        mGridLayoutManager = GridLayoutManager(requireContext(), 8)
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
            //Client.global.eventBus.postEvent(MessageCreateEvent(it))
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

class MessageListOnScrollListener :
    RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == 2 && canScrollUp(recyclerView)) {

        }
    }

    /** return true if the list can't go up */
    fun canScrollUp(recyclerView: RecyclerView): Boolean {
        return !recyclerView.canScrollVertically(-1)
    }

    /** return true if the list can't go down */
    fun canScrollDown(recyclerView: RecyclerView): Boolean {
        return !recyclerView.canScrollVertically(1)
    }

}