package cn.troph.tomon.ui.chat.emoji

import cn.troph.tomon.core.structures.GuildEmoji
import com.google.gson.annotations.SerializedName

data class CustomGuildEmoji(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val isBuildIn: Boolean = false,
    val emojiList: MutableList<GuildEmoji> = mutableListOf(),
    val systemEmojiList: MutableList<Int> = mutableListOf(),
    val systemEmojiListData: MutableList<SystemEmojiData> = mutableListOf()
)

data class SystemEmojiData(
    @SerializedName("unified") val code: String,
    @SerializedName("category") val category: String,
    @SerializedName("image")val image:String
)