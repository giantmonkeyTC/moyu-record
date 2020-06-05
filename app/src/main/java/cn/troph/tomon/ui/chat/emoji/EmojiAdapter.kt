package cn.troph.tomon.ui.chat.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.troph.tomon.R
import com.bumptech.glide.Glide
import com.cruxlab.sectionedrecyclerview.lib.BaseSectionAdapter
import com.cruxlab.sectionedrecyclerview.lib.SectionAdapter
import kotlinx.android.synthetic.main.emoji_image.view.*
import kotlinx.android.synthetic.main.emoji_item.view.*


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
        holder?.itemView?.guid_name_emoji?.text = emojiSectionObj.name
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
                } else {
                    it.textview_emoji?.visibility = View.GONE
                    it.imageview_emoji?.visibility = View.VISIBLE
                    Glide.with(it.context).load(emojiSectionObj.emojiList[position].url)
                        .into(it.imageview_emoji)
                }
            }
        }
    }

    class EmojiItemViewHolder(itemView: View) : BaseSectionAdapter.ItemViewHolder(itemView)
    class EmojiHeaderViewHolder(itemView: View) : BaseSectionAdapter.HeaderViewHolder(itemView)
}


interface OnEmojiClickListener {
    fun onEmojiSelected(emojiCode: String)
    fun onSystemEmojiSelected(unicode: Int)
}