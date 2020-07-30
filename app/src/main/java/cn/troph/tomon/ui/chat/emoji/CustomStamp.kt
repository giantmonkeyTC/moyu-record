package cn.troph.tomon.ui.chat.emoji

import cn.troph.tomon.core.structures.GuildEmoji
import cn.troph.tomon.core.structures.Stamp
import com.google.gson.annotations.SerializedName

data class CustomGuildStamp(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val isBuildIn: Boolean = false,
    val stampList: MutableList<Stamp> = mutableListOf(),
    val systemStampList: MutableList<Int> = mutableListOf(),
    val systemStampListData: MutableList<SystemStampData> = mutableListOf()
)

data class SystemStampData(
    @SerializedName("unified") val code: String,
    @SerializedName("category") val category: String,
    @SerializedName("image")val image:String
)