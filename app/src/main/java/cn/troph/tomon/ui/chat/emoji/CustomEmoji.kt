package cn.troph.tomon.ui.chat.emoji

import cn.troph.tomon.core.structures.GuildEmoji

data class CustomGuildEmoji(
    val id: String = "",
    val name: String = "",
    val code:String = "",
    val isBuildIn: Boolean = false,
    val emojiList: MutableList<GuildEmoji> = mutableListOf(),
    val systemEmojiList:MutableList<Int> = mutableListOf()
)