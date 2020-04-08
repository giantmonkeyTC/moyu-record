package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonData
import com.google.gson.JsonArray
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface MessageService {
    @GET("channels/{channelId}/messages")
    suspend fun getMessages(@Path("channelId") channelId: String, @Header("Authorization") token: String): JsonArray

    data class CreateMessageRequest(
        var content: String
    )

    @POST("channels/{channelId}/messages")
    fun createMessage(
        @Path("channelID") channelId: String, @Body request: CreateMessageRequest, @Header(
            "Authorization"
        ) token: String
    ): JsonData

    @Multipart
    @POST("channels/{channelId}/messages")
    suspend fun uploadAttachments(
        @Path("channelID") channelId: String, @PartMap partMap: Map<String, RequestBody>, @Part vararg files: MultipartBody.Part, @Header(
            "Authorization"
        ) token: String
    ): JsonData

    @DELETE("channels/{channelId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("channelId") channelId: String, @Path("messageId") messageId: String, @Header(
            "Authorization"
        ) token: String
    ): Void

    data class UpdateMessageRequest(
        val content: String
    )

    @PATCH("channels/{channelId}/messages/{messageId}")
    suspend fun updateMessage(
        @Path("channelId") channelId: String, @Path("messageId") messageId: String, @Body request: UpdateMessageRequest, @Header(
            "Authorization"
        ) token: String
    ): JsonArray

    @PUT("channels/{channelId}/messages/{messageId}/reactions/{identifier}/@me")
    suspend fun createReaction(
        @Path("channelId") channelId: String, @Path("messageId") messageId: String, @Path("identifier") identifier: String, @Header(
            "Authorization"
        ) token: String
    ): JsonArray

}