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
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.emoji_image.view.*
import kotlinx.android.synthetic.main.emoji_item.view.*
import kotlinx.android.synthetic.main.item_bottom_emoji_icon.view.*
import java.lang.NumberFormatException
import java.lang.StringBuilder
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

                    it.textview_emoji.text =
                        parseEmoji(emojiSectionObj.systemEmojiListData[position])

                    it.textview_emoji.setOnClickListener {
                        emojiClickListener.onSystemEmojiSelected(parseEmoji(emojiSectionObj.systemEmojiListData[holder.sectionAdapterPosition]))
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

    private fun parseEmoji(emojiObj: SystemEmojiData): String {
        val charArray = emojiObj.code.split(
            "-"
        )
        val newString = charArray.map {
            it.toInt(16)
        }
        val sb = StringBuilder()
        for (item in newString) {
            val char = Character.toChars(item)
            sb.append(char)
        }
        return sb.toString()
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
            urlList[position].text?.let {
                try {
                    holder.itemView.ctv.text = parseEmoji(it)
                } catch (e: NumberFormatException) {
                    holder.itemView.ctv.text = it[0].toString()
                }

            }
        }

        holder.itemView.setOnClickListener {
            onBottomGuildSelectedListener.onGuildSelected(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return urlList.size
    }

    private fun parseEmoji(emojiObj: String): String {
        val charArray = emojiObj.split(
            "-"
        )
        val newString = charArray.map {
            it.toInt(16)
        }
        val sb = StringBuilder()
        for (item in newString) {
            val char = Character.toChars(item)
            sb.append(char)
        }
        return sb.toString()
    }

    class BottomEmojiVH(itemView: View) : RecyclerView.ViewHolder(itemView)
}

data class GuildIcon(
    @SerializedName("url") val url: String?,
    @SerializedName("text") val text: String?
)

interface OnBottomGuildSelectedListener {
    fun onGuildSelected(position: Int)
}


interface OnEmojiClickListener {
    fun onEmojiSelected(emojiCode: String)
    fun onSystemEmojiSelected(unicode: String)
}