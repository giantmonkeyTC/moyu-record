package cn.troph.tomon.ui.chat.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.emoji_item.view.*

class EmojiAdapter(private val emojiList: MutableList<CustomGuildEmoji>) :
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
                val imageView = ImageView(holder.itemView.context)
                holder.itemView.item_holder_emoji.addView(imageView)
                Glide.with(imageView).load(item.url).into(imageView)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        return EmojiViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.emoji_item, parent, false)
        )
    }
}