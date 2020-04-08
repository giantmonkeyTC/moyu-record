package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface ChannelService {

    @GET("guilds/{id}/channels")
    fun getGuildChannels(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<JsonArray>

    @GET("channels/{id}")
    fun getChannel(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<JsonData>

    data class CreateGuildChannelRequest(
        val name: String,
        val type: Int,
        val parent_id: String?,
        val default_message_notifications: Int
    )

    @POST("guilds/{id}/channels")
    fun createGuildChannel(
        @Path("id") id: String,
        @Body request: CreateGuildChannelRequest,
        @Header("Authorization") token: String
    ): Observable<JsonData>

    data class PermissionOverwrites(
        val id: String,
        val type: String,
        val allow: Int,
        val deny: Int
    )

    data class ModifyChannelRequest(
        val name: String?,
        val parent_id: String?,
        val topic: String?,
        val permission_overwrites: List<PermissionOverwrites>?,
        val default_message_notifications: Int?
    )

    @PATCH("channels/{id}")
    fun modifyChannel(
        @Path("id") id: String,
        @Body request: ModifyChannelRequest,
        @Header("Authorization") token: String
    ): Observable<JsonData>

    @DELETE("channels/{id}")
    fun deleteChannel(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<Void>

    data class UpsertPermissionOverwritesRequest(
        val type: String?,
        val allow: Int?,
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
}