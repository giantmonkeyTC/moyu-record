package cn.troph.tomon.ui.chat.emoji

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_reaction.*
import kotlinx.android.synthetic.main.fragment_reaction.bottom_emoji_rr


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EmojiFragment(val onEmojiClickListener: OnEmojiClickListener) : Fragment() {

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
        loadEmoji(onEmojiClickListener = onEmojiClickListener)
    }

    private fun loadEmoji(onEmojiClickListener: OnEmojiClickListener) {
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
            guildIcon.add(GuildIcon(item.iconURL, item.name, null))
            val sectionAdapter = EmojiAdapter(sectionData, onEmojiClickListener)
            mSectionDataManager.addSection(sectionAdapter, 1)
        }
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
        //load system emoji
        val systemEmoji = SystemEmoji(requireContext())
        for (item in systemEmoji.returnEmojiWithCategory()) {
            val adapter = EmojiAdapter(
                CustomGuildEmoji(
                    name = item.key,
                    isBuildIn = true,
                    systemEmojiListData = item.value
                ), onEmojiClickListener
            )
            mSectionDataManager.addSection(adapter, 1)
//            guildIcon.add(GuildIcon(null, item.value[0].code,null))
        }
        reaction_rr.adapter = mSectionDataManager.adapter
        reaction_section_header_layout.attachTo(reaction_rr, mSectionDataManager)
        guildIcon[0].isHighLight = true
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
        val mBottomLLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        bottom_emoji_rr.layoutManager = mBottomLLayoutManager
        bottom_emoji_rr.adapter = mBottomEmojiAdapter
        val handler = Handler()
        handler.postDelayed({
            val view = bottom_emoji_rr.findViewHolderForAdapterPosition(0)
            view?.itemView?.performClick()
        }, 300)
        reaction_rr.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position =
                        mSectionDataManager.calcSection(mGridLayoutManager.findFirstCompletelyVisibleItemPosition())
                    bottom_emoji_rr.smoothScrollToPosition(position)
                    guildIcon.forEach {
                        it.isHighLight = false
                    }
                    guildIcon[position].isHighLight = true
                    mBottomEmojiAdapter.notifyDataSetChanged()
                }
            }
        })
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
