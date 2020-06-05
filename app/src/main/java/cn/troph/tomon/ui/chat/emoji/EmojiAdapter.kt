package cn.troph.tomon.ui.chat.emoji

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.emoji.widget.EmojiTextView
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.emoji_item.view.*


class EmojiAdapter(
    private val emojiList: MutableList<CustomGuildEmoji>,
    private val emojiClickListener: onEmojiClickListener
) :
    RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {
    class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemCount(): Int {
        return emojiList.size
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val emoji = emojiList[position]
        if (!emoji.isBuildIn) {
            holder.itemView.guid_name_emoji.text = emoji.name
            for (item in emoji.emojiList) {
                val iv = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.emoji_image, null, false) as ImageView
                holder.itemView.item_holder_emoji.addView(
                    iv,
                    ViewGroup.LayoutParams(
                        100,
                        100
                    )
                )
                iv.setOnClickListener {
                    emojiClickListener.onEmojiSelected(
                        "<%${emoji.emojiList[holder.itemView.item_holder_emoji.indexOfChild(
                            it
                        )].name}:${emoji.emojiList[holder.itemView.item_holder_emoji.indexOfChild(
                            it
                        )].id}>"
                    )
                }
                Glide.with(iv).load(item.url).into(iv)
            }
        } else {
            holder.itemView.guid_name_emoji.text = emoji.name
            for (item in emoji.systemEmojiList) {
                val iv = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.emoji_tv, null, false) as EmojiTextView
                holder.itemView.item_holder_emoji.addView(
                    iv,
                    ViewGroup.LayoutParams(
                        100,
                        100
                    )
                )
                iv.setOnClickListener {
                    emojiClickListener.onEmojiSelected(
                        emoji.systemEmojiList[holder.itemView.item_holder_emoji.indexOfChild(
                            it
                        )].toString()
                    )
                }
                iv.text = String(Character.toChars(item))
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {

        return EmojiViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.emoji_item, parent, false)
        )


    }
}


interface onEmojiClickListener {
    fun onEmojiSelected(emojiCode: String)
}