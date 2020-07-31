package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class StampPack(
    @SerializedName("author_id") val authorId: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("stamps") val stamps: MutableList<Stamp>
)