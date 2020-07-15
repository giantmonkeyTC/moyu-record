package cn.troph.tomon.core.network.services

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface GuildMemberService {
    @GET("channels/{channelId}/members")
    fun getMembers(
        @Path("channelId") channelId: String,
        @Header("Authorization") token: String
    ): Observable<JsonArray>

    @POST("users/@me/reports")
    fun reportMember(@Body member: ReportMember): Observable<Any>

    data class ReportMember(
        @SerializedName("target_id") val targetId: String,
        @SerializedName("reason") val reason: Int,
        @SerializedName("type") val type: Int,
        @SerializedName("comment") val comment: String? = ""
    )
}