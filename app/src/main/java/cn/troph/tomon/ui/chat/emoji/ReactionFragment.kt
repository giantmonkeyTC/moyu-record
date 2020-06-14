package cn.troph.tomon.ui.chat.emoji

import android.os.Bundle
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_reaction.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReactionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReactionFragment : BottomSheetDialogFragment() {

    lateinit var mMessage: cn.troph.tomon.core.structures.Message
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mSectionDataManager: SectionDataManager
    private lateinit var mGridLayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    fun setMessage(msg: cn.troph.tomon.core.structures.Message) {
        mMessage = msg
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadEmoji()
    }

    private fun loadEmoji() {
        val emojiClickListener = object : OnEmojiClickListener {
            override fun onEmojiSelected(emojiCode: String) {
                val modifyedEmoji = emojiCode.removePrefix("<%").removeSuffix(">")
                Client.global.rest.messageService.addReaction(
                    mMessage.channelId,
                    mMessage.id!!,
                    modifyedEmoji,
                    Client.global.auth
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ -> Logger.d("success") },
                        { throwable ->
                            Logger.d(throwable.message)
                        })
                dismiss()
            }

            override fun onSystemEmojiSelected(unicode: Int) {
                Client.global.rest.messageService.addReaction(
                    mMessage.channelId,
                    mMessage.id!!,
                    String(Character.toChars(unicode)),
                    Client.global.auth
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ -> Logger.d("success") },
                        { throwable ->
                            Logger.d(throwable.message)
                        })
                dismiss()
            }
        }
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
        reaction_rr.layoutManager = mGridLayoutManager

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

            val sectionAdapter = EmojiAdapter(sectionData, emojiClickListener)
            mSectionDataManager.addSection(sectionAdapter, 1)
        }
        val systemEmoji = SystemEmoji()
        mSectionDataManager.addSection(
            EmojiAdapter(
                systemEmoji.getSystemEmojiEmoticons(),
                emojiClickListener
            ), 1
        )
        mSectionDataManager.addSection(
            EmojiAdapter(
                systemEmoji.getSystemEmojiDingbats(),
                emojiClickListener
            ), 1
        )
        mSectionDataManager.addSection(
            EmojiAdapter(
                systemEmoji.getSystemEmojiTransport(),
                emojiClickListener
            ), 1
        )

        reaction_rr.adapter = mSectionDataManager.adapter
        reaction_section_header_layout.attachTo(reaction_rr, mSectionDataManager)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReactionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReactionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
