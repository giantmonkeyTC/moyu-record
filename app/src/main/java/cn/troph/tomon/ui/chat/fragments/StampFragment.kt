package cn.troph.tomon.ui.chat.fragments

import BottomStampAdapter
import OnBottomPackSelectedListener
import OnStampClickListener
import StampAdapter
import StampIcon
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.Stamp
import cn.troph.tomon.core.structures.StampPack
import cn.troph.tomon.core.utils.SnowFlakesGenerator
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_reaction.*
import kotlinx.android.synthetic.main.fragment_stamp.*
import java.time.LocalDateTime

class StampFragment : Fragment() {
    private val mChatVM: ChatSharedViewModel by activityViewModels()
    private val stampPacks: MutableList<StampPack> = mutableListOf()
    private lateinit var mSectionDataManager: SectionDataManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stamp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadStamp()
    }

    data class stampParams(
        @SerializedName("nonce") val nonce: String,
        @SerializedName("stamps") val stamps: MutableList<String>
    )

    private fun loadStamp() {
        val stampPackIcon = mutableListOf<StampIcon>()
        val stampClickListener = object : OnStampClickListener {
            override fun onStampSelected(stamps: MutableList<Stamp>) {
                AppState.global.channelSelection.value.channelId?.let {
                    val emptyMsg = createEmptyMsg(null)
                    emptyMsg.stamps.add(
                        stamps[0]
                    )
                    mChatVM.stampSendedLiveData.value =
                        ChatSharedViewModel.StampSendedState(
                            state = true,
                            emptyMsg = emptyMsg
                        )
                    Client.global.rest.messageService.createStampMessage(
                        channelId = it,
                        jsonObject =
                        JsonParser.parseString(
                            Gson().toJson(
                                stampParams(
                                    nonce = emptyMsg.nonce.toString(),
                                    stamps = mutableListOf(stamps[0].id)
                                )
                            )
                        ).asJsonObject
                        ,
                        token = Client.global.auth
                    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ _ ->

                        }
                            , { error ->
                                Log.d("nope", error.message)
                                Toast.makeText(
                                    requireContext(),
                                    R.string.send_fail,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            })
                }

            }
        }
        mChatVM.stampsLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                stampPacks.clear()
                stampPacks.addAll(it)
                stampPacks.forEach {
                    stampPackIcon.add(StampIcon(text = it.name))
                    stampPackIcon[0].isHighLight = true
                    val sectionAdapter = StampAdapter(it, stampClickListener)
                    mSectionDataManager.addSection(sectionAdapter, 1)
                }
                stamp_rr.adapter = mSectionDataManager.adapter

                stampPackIcon.removeAt(stampPackIcon.size - 1)
                stampPackIcon.add(
                    StampIcon(
                        text = null,
                        drawable = resources.getDrawable(R.drawable.ic_default_stamp_pack)
                    )
                )
                mSectionDataManager.adapter.notifyDataSetChanged()
            }
        })

        mChatVM.loadStamps()
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
        stamp_rr.layoutManager = mGridLayoutManager
        stamp_section_header_layout.attachTo(stamp_rr, mSectionDataManager)

        val mBottomStampAdapter =
            BottomStampAdapter(stampPackIcon, onBottomPackSelectedListener = object :
                OnBottomPackSelectedListener {
                override fun onPackSelected(position: Int) {
                    mGridLayoutManager.scrollToPosition(
                        mSectionDataManager.calcAdapterPos(
                            position,
                            0
                        ) - 1
                    )
                }
            })
        val mBottomLLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        bottom_stamp_rr.layoutManager = mBottomLLayoutManager
        bottom_stamp_rr.adapter = mBottomStampAdapter
        val handler = Handler()
        handler.postDelayed({
            val view = bottom_stamp_rr.findViewHolderForAdapterPosition(0)
            view?.itemView?.performClick()
        }, 300)
        stamp_rr.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position =
                        mSectionDataManager.calcSection(mGridLayoutManager.findFirstCompletelyVisibleItemPosition())
                    bottom_stamp_rr.smoothScrollToPosition(position)
                    stampPackIcon.forEach {
                        it.isHighLight = false
                    }
                    stampPackIcon[position].isHighLight = true
                    mBottomStampAdapter.notifyDataSetChanged()
                }
            }
        })

    }

    private fun createEmptyMsg(content: String?): Message {
        val msgObject = JsonObject()
        msgObject.addProperty("id", "")
        msgObject.addProperty("nonce", SnowFlakesGenerator(1).nextId())
        msgObject.addProperty("channelId", AppState.global.channelSelection.value.channelId)
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
}