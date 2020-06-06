package cn.troph.tomon.ui.chat.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.utils.url
import com.bumptech.glide.Glide
import com.cruxlab.sectionedrecyclerview.lib.BaseSectionAdapter
import com.cruxlab.sectionedrecyclerview.lib.SectionAdapter
import kotlinx.android.synthetic.main.emoji_image.view.*
import kotlinx.android.synthetic.main.emoji_item.view.*
import kotlinx.android.synthetic.main.item_bottom_emoji_icon.view.*


class EmojiAdapter(
    private val emojiSectionObj: CustomGuildEmoji,
    private val emojiClickListener: OnEmojiClickListener
) : SectionAdapter<EmojiAdapter.EmojiItemViewHolder, EmojiAdapter.EmojiHeaderViewHolder>(
    true,
    true
) {
    override fun onCreateItemViewHolder(parent: ViewGroup?, type: Short): EmojiItemViewHolder {
        return EmojiItemViewHolder(
            LayoutInflater.from(parent?.context!!).inflate(R.layout.emoji_image, parent, false)
        )
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): EmojiHeaderViewHolder {
        return EmojiHeaderViewHolder(
            LayoutInflater.from(parent?.context!!).inflate(R.layout.emoji_item, parent, false)
        )
    }

    override fun onBindHeaderViewHolder(holder: EmojiHeaderViewHolder?) {
        holder?.itemView?.guid_name_emoji?.text = "#${emojiSectionObj.name}#"
    }

    override fun getItemCount(): Int {
        return if (emojiSectionObj.isBuildIn) emojiSectionObj.systemEmojiList.size else emojiSectionObj.emojiList.size
    }

    override fun onBindItemViewHolder(holder: EmojiItemViewHolder?, position: Int) {
        holder?.let {
            it.itemView?.let {
                if (emojiSectionObj.isBuildIn) {
                    it.textview_emoji.visibility = View.VISIBLE
                    it.imageview_emoji.visibility = View.GONE
                    it.textview_emoji.text =
                        String(Character.toChars(emojiSectionObj.systemEmojiList[position]))
                    it.textview_emoji.setOnClickListener {
                        emojiClickListener.onSystemEmojiSelected(emojiSectionObj.systemEmojiList[holder.sectionAdapterPosition])
                    }
                } else {
                    it.textview_emoji?.visibility = View.GONE
                    it.imageview_emoji?.visibility = View.VISIBLE
                    Glide.with(it.context).load(emojiSectionObj.emojiList[position].url)
                        .into(it.imageview_emoji)
                    it.imageview_emoji.setOnClickListener {
                        emojiClickListener.onEmojiSelected("<%${emojiSectionObj.emojiList[holder.sectionAdapterPosition].name}:${emojiSectionObj.emojiList[holder.sectionAdapterPosition].id}>")
                    }
                }
            }
        }
    }

    class EmojiItemViewHolder(itemView: View) : BaseSectionAdapter.ItemViewHolder(itemView)
    class EmojiHeaderViewHolder(itemView: View) : BaseSectionAdapter.HeaderViewHolder(itemView)
}


class BottomEmojiAdapter(
    private val urlList: MutableList<String>,
    private val onBottomGuildSelectedListener: OnBottomGuildSelectedListener
) :
    RecyclerView.Adapter<BottomEmojiAdapter.BottomEmojiVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomEmojiVH {
        return BottomEmojiVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bottom_emoji_icon, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BottomEmojiVH, position: Int) {
        Glide.with(holder.itemView).load(urlList[position]).into(holder.itemView.bottom_emoji_iv)
        holder.itemView.setOnClickListener {
            onBottomGuildSelectedListener.onGuildSelected(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return urlList.size
    }

    class BottomEmojiVH(itemView: View) : RecyclerView.ViewHolder(itemView)
}

interface OnBottomGuildSelectedListener {
    fun onGuildSelected(position: Int)
}


interface OnEmojiClickListener {
    fun onEmojiSelected(emojiCode: String)
    fun onSystemEmojiSelected(unicode: Int)
}