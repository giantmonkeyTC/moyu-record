package cn.troph.tomon.ui.chat.fragments

import OnStampClickListener
import StampAdapter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.MessageService
import cn.troph.tomon.core.structures.StampPack
import cn.troph.tomon.core.utils.SnowFlakesGenerator
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import com.google.gson.Gson
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_stamp.*
import org.json.JSONObject

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
        val stampClickListener = object : OnStampClickListener {
            override fun onStampSelected(stamps: MutableList<String>) {
                AppState.global.channelSelection.value.channelId?.let {
                    Log.d(
                        "hello", Gson().toJson(
                            stampParams(
                                nonce = SnowFlakesGenerator(1).nextId().toString(),
                                stamps = stamps
                            )
                        )
                    )
                    Client.global.rest.messageService.createStampMessage(
                        channelId = it,
                        jsonObject =
                        JsonParser.parseString(
                            Gson().toJson(
                                stampParams(
                                    nonce = SnowFlakesGenerator(1).nextId().toString(),
                                    stamps = stamps
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
                    val sectionAdapter = StampAdapter(it, stampClickListener)
                    mSectionDataManager.addSection(sectionAdapter, 1)
                }
                stamp_rr.adapter = mSectionDataManager.adapter
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
    }

}