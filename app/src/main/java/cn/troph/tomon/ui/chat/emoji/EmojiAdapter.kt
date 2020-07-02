package cn.troph.tomon.ui.chat.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import com.bumptech.glide.Glide
import com.cruxlab.sectionedrecyclerview.lib.BaseSectionAdapter
import com.cruxlab.sectionedrecyclerview.lib.SectionAdapter
import kotlinx.android.synthetic.main.emoji_image.view.*
import kotlinx.android.synthetic.main.emoji_item.view.*
import kotlinx.android.synthetic.main.item_bottom_emoji_icon.view.*
import java.nio.ByteBuffer


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
        return if (emojiSectionObj.isBuildIn) emojiSectionObj.systemEmojiListData.size else emojiSectionObj.emojiList.size
    }

    override fun onBindItemViewHolder(holder: EmojiItemViewHolder?, position: Int) {
        holder?.let {
            it.itemView?.let {
                if (emojiSectionObj.isBuildIn) {
                    it.textview_emoji.visibility = View.VISIBLE
                    it.imageview_emoji.visibility = View.GONE
                    val charArray = emojiSectionObj.systemEmojiListData[position].code.split("-")
                    val newString = charArray.map {
                        "0x${it}"
                    }.joinToString(separator = "-")

//                    val bytes =
//                        ByteBuffer.allocate(newString.size * Long.SIZE_BYTES)
//                    for (item in newString) {
//                        bytes.putLong(item)
//                    }
                    it.textview_emoji.text = String(newString.toCharArray())
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
    private val urlList: MutableList<GuildIcon>,
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
        if (urlList[position].url != null) {
            holder.itemView.bottom_emoji_iv.visibility = View.VISIBLE
            holder.itemView.ctv.visibility = View.GONE
            Glide.with(holder.itemView).load(urlList[position].url)
                .into(holder.itemView.bottom_emoji_iv)

        } else {
            holder.itemView.bottom_emoji_iv.visibility = View.GONE
            holder.itemView.ctv.visibility = View.VISIBLE
            holder.itemView.ctv.text = urlList[position].text!![0].toString()
        }

        holder.itemView.setOnClickListener {
            onBottomGuildSelectedListener.onGuildSelected(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return urlList.size
    }

    class BottomEmojiVH(itemView: View) : RecyclerView.ViewHolder(itemView)
}

data class GuildIcon(val url: String?, val text: String?)

interface OnBottomGuildSelectedListener {
    fun onGuildSelected(position: Int)
}


interface OnEmojiClickListener {
    fun onEmojiSelected(emojiCode: String)
    fun onSystemEmojiSelected(unicode: Int)
}