package cn.troph.tomon.ui.chat.emoji

import cn.troph.tomon.core.structures.Guild
import com.vanniktech.emoji.emoji.Emoji
import com.vanniktech.emoji.emoji.EmojiCategory

class GuildEmojiCategory(private val guild: Guild) : EmojiCategory {

    override fun getEmojis(): Array<Emoji> {
        val emoji = mutableListOf<Emoji>()
        return emoji.toTypedArray()
    }

    override fun getIcon(): Int {
        return 0
    }


}