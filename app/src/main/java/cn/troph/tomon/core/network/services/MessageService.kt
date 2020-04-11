package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonData
import com.google.gson.JsonArray
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


interface MessageService {
    @GET("channels/{channelId}/messages")
    fun getMessages(
        @Path("channelId") channelId: String,
        @Header("Authorization") token: String
    ): Observable<JsonArray>

    @GET("channels/{channelId}/messages/{messageId}")
    fun getMessage(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String,
        @Header("Authorization") token: String
    ): Observable<JsonData>

    data class CreateMessageRequest(
        var content: String
    )

    @POST("channels/{channelId}/messages")
    fun createMessage(
        @Path("channelID") channelId: String, @Body request: CreateMessageRequest, @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonData>

    @Multipart
    @POST("channels/{channelId}/messages")
    fun uploadAttachments(
        @Path("channelID") channelId: String,
        @PartMap partMap: Map<String, RequestBody>,
        @Part vararg files: MultipartBody.Part,
        @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonData>

    @DELETE("channels/{channelId}/messages/{messageId}")
    fun deleteMessage(
        @Path("channelId") channelId: String, @Path("messageId") messageId: String, @Header(
            "Authorization"
        ) token: String
    ): Observable<Void>

    data class UpdateMessageRequest(
        val content: String
    )

    @PATCH("channels/{channelId}/messages/{messageId}")
    fun updateMessage(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String,
        @Body request: UpdateMessageRequest,
        @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonArray>

    @PUT("channels/{channelId}/messages/{messageId}/reactions/{identifier}/@me")
    fun addReaction(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String,
        @Path("identifier") identifier: String,
        @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonArray>

}