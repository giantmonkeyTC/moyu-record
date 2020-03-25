package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonData
import com.google.gson.JsonArray
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface MessageService {
    @GET("channels/{channelId}/messages")
    fun getMessages(@Path("channelId") channelId: String, @Header("Authorization") token: String): Call<JsonArray>

    data class createMessageRequest(
        var content: String
    )

    @POST("channels/{channelId}/messages")
    fun createMessage(
        @Path("channelID") channelId: String, @Body request: createMessageRequest, @Header(
            "Authorization"
        ) token: String
    ): Call<JsonData>

    @Multipart
    @POST("channels/{channelId}/messages")
    fun uploadAttachments(
        @Path("channelID") channelId: String, @PartMap partMap: Map<String, RequestBody>, @Part vararg files: MultipartBody.Part, @Header(
            "Authorization"
        ) token: String
    ): Call<JsonData>

    @DELETE("channels/{channelId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("channelId") channelId: String, @Path("messageId") messageId: String, @Header(
            "Authorization"
        ) token: String
    ): Call<Void>

    data class updateMessageRequest(
        val content: String
    )

    @PATCH("channels/{channelId}/messages/{messageId}")
    fun updateMessage(
        @Path("channelId") channelId: String, @Path("messageId") messageId: String, @Body request: updateMessageRequest, @Header(
            "Authorization"
        ) token: String
    ): Call<JsonArray>

    @PUT("channels/{channelId}/messages/{messageId}/reactions/{identifier}/@me")
    fun createReaction(
        @Path("channelId") channelId: String, @Path("messageId") messageId: String, @Path("identifier") identifier: String, @Header(
            "Authorization"
        ) token: String
    ): Call<JsonArray>


}