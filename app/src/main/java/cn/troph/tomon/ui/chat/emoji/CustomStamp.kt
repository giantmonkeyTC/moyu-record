package cn.troph.tomon.ui.chat.emoji

import cn.troph.tomon.core.structures.GuildEmoji
import cn.troph.tomon.core.structures.Stamp
import com.google.gson.annotations.SerializedName

data class CustomGuildStamp(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val isBuildIn: Boolean = false,
    val stampList: MutableList<Stamp> = mutableListOf()
)