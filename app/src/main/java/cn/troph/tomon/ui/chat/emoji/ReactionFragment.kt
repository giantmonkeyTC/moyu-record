package cn.troph.tomon.ui.chat.emoji

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_reaction.*


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ReactionFragment : BottomSheetDialogFragment() {

    lateinit var mMessage: cn.troph.tomon.core.structures.Message
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mSectionDataManager: SectionDataManager
    private lateinit var mGridLayoutManager: GridLayoutManager

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val bottomSheetDialog =
//            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
//            val dialogc = dialog as BottomSheetDialog
//            // When using AndroidX the resource can be found at com.google.android.material.R.id.design_bottom_sheet
//            val bottomSheet =
//                dialogc.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
//            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet as View)
//            bottomSheetBehavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
//        }
//        return bottomSheetDialog
//    }

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
                    .subscribe({ _ -> },
                        { _ ->

                        })
                dismiss()
            }

            override fun onSystemEmojiSelected(unicode: String) {
                Client.global.rest.messageService.addReaction(
                    mMessage.channelId,
                    mMessage.id!!,
                    unicode,
                    Client.global.auth
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ -> Logger.d("success") },
                        { throwable ->
                            Logger.d(throwable.message)
                        })
                dismiss()
            }
        }

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
        reaction_rr.layoutManager = mGridLayoutManager
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
            item.iconURL?.let {
                guildIcon.add(GuildIcon(url = it, text = item.name))
            }

            val sectionAdapter = EmojiAdapter(sectionData, emojiClickListener)
            mSectionDataManager.addSection(sectionAdapter, 1)
        }
        //load system emoji
        val systemEmoji = SystemEmoji(requireContext())
        for (item in systemEmoji.returnEmojiWithCategory()) {
            val adapter = EmojiAdapter(
                CustomGuildEmoji(
                    name = item.key,
                    isBuildIn = true,
                    systemEmojiListData = item.value
                ), emojiClickListener
            )
            mSectionDataManager.addSection(adapter, 1)
            guildIcon.add(GuildIcon(null, item.value[0].code))
        }

        reaction_rr.adapter = mSectionDataManager.adapter
        reaction_section_header_layout.attachTo(reaction_rr, mSectionDataManager)
        // bottom Emoji
        val mBottomEmojiAdapter = BottomEmojiAdapter(
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
