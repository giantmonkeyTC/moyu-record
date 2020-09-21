package cn.troph.tomon.core.actions

import com.google.gson.annotations.SerializedName

data class Position(
    @SerializedName("id") val id: String,
    @SerializedName("position") val position: Int,
    @SerializedName("parent_id") var parent_id: String?
)