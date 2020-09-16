package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.structures.DmChannel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.*

interface ChannelService {

    @GET("users/@me/channels")
    fun getDmChannels(
        @Header("Authorization") token: String
    ): Observable<MutableList<DmChannel>>

    data class CreateDmChannelRequest(
        @SerializedName("recipients")
        val recipients: MutableList<String>
    )

    @POST("users/@me/channels")
    fun createDmChannel(
        @Header("Authorization") token: String,
        @Body request: CreateDmChannelRequest
    ): Observable<JsonObject>

    @GET("guilds/{id}/channels")
    fun getGuildChannels(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<JsonArray>

    @GET("channels/{id}")
    fun getChannel(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    data class CreateGuildChannelRequest(
        @SerializedName("name")
        val name: String,
        @SerializedName("type")
        val type: Int,
        @SerializedName("parent_id")
        val parent_id: String?,
        @SerializedName("default_message_notification")
        val default_message_notifications: Int
    )

    @POST("guilds/{id}/channels")
    fun createGuildChannel(
        @Path("id") id: String,
        @Body request: CreateGuildChannelRequest,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    data class PermissionOverwrites(
        @SerializedName("id")
        val id: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("allow")
        val allow: Int,
        @SerializedName("deny")
        val deny: Int
    )

    data class ModifyChannelRequest(
        @SerializedName("name")
        val name: String?,
        @SerializedName("parent_id")
        val parent_id: String?,
        @SerializedName("topic")
        val topic: String?,
        @SerializedName("permission_overwrites")
        val permission_overwrites: List<PermissionOverwrites>?,
        @SerializedName("default_message_notification")
        val default_message_notifications: Int?
    )

    @PATCH("channels/{id}")
    fun modifyChannel(
        @Path("id") id: String,
        @Body request: ModifyChannelRequest,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    @DELETE("channels/{id}")
    fun deleteChannel(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<Void>

    data class UpsertPermissionOverwritesRequest(
        @SerializedName("type")
        val type: String?,
        @SerializedName("allow")
        val allow: Int?,
        @SerializedName("deny")
        val deny: Int?
    )

    @PUT("channels/{channelId}/permissions/{id}")
    fun upsertPermissionOverwrites(
        @Path("channelId") channelId: String,
        @Path("id") id: String,
        @Body request: UpsertPermissionOverwritesRequest,
        @Header("Authorization") token: String
    ): Observable<Void>

    @DELETE("channels/{channelId}/permissions/{id}")
    fun deletePermissionOverwrites(
        @Path("channelId") channelId: String,
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<Void>

    data class AckChannel (
        @SerializedName("channel_id")
        val channel_id: String,
        @SerializedName("message_id")
        val message_id: String
    )

    data class ChannelsBulkAcks(
        @SerializedName("acks")
        val acks:List<AckChannel>
    )

    @POST("channels/bulk-acks")
    fun bulkAcks(
        @Header("Authorization") token: String,
        @Body request:ChannelsBulkAcks
    ):Observable<Response<JsonObject>>
}