package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.structures.GuildSettingsOverride
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.*

interface GuildService {

    @GET("users/@me/guilds")
    fun getGuilds(@Header("Authorization") token: String): Observable<JsonArray>;

    @GET("guilds/{id}")
    fun getGuild(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<JsonObject>;

    data class CreateGuildRequest(
        @SerializedName("name")
        val name: String,
        @SerializedName("icon")
        val icon: String
    )

    @POST("guilds")
    fun createGuild(
        @Body request: CreateGuildRequest,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    data class ModifyGuildRequest(
        @SerializedName("name")
        val name: String?,
        @SerializedName("icon")
        val icon: String?
    )

    @PATCH("guilds/{id}")
    fun modifyGuild(
        @Path("id") id: String,
        @Body request: ModifyGuildRequest,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    data class NotifyGuildSetting(
        @SerializedName("mute")
        val mute: Boolean?,
        @SerializedName("message_notifications")
        val message_notifications: Int,
        @SerializedName("suppress_everyone")
        val suppress_everyone: Boolean,
        @SerializedName("channel_overrides")
        val channel_overrides: JsonArray
    )

    @PATCH("users/@me/guilds/{id}/settings")
    fun setNotifyGuild(
        @Path("id") id: String,
        @Body setting: NotifyGuildSetting,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    @DELETE("guilds/{id}")
    fun deleteGuild(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<Void>

    @DELETE("users/@me/guilds/{id}")
    fun leaveGuild(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<Response<Integer>>

    data class SetNickNameRequest(
        @SerializedName("nick")
        val nick: String
    )

    @PATCH("guilds/{id}/members/@me/nick")
    fun setNickName(
        @Path("id") id: String,
        @Header("Authorization") token: String,
        @Body request:SetNickNameRequest
    ):Observable<JsonObject>

}