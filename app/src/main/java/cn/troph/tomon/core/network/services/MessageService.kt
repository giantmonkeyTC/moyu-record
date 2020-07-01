package cn.troph.tomon.core.network.services

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface MessageService {

    @GET("channels/{channelId}/messages")
    fun getMessages(
        @Path("channelId") channelId: String,
        @Query("before") before: String? = null,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null,
        @Header("Authorization") token: String
    ): Observable<JsonArray>

    @GET("channels/{channelId}/messages/{messageId}")
    fun getMessage(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    data class CreateMessageRequest(
        @SerializedName("content")
        var content: String,
        @SerializedName("nonce")
        var nonce:String
    )

    @POST("channels/{channelId}/messages")
    fun createMessage(
        @Path("channelId") channelId: String, @Body request: CreateMessageRequest, @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonObject>

    @Multipart
    @POST("channels/{channelId}/messages")
    fun uploadAttachments(
        @Path("channelId") channelId: String,
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part files: MultipartBody.Part,
        @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonObject>

    @DELETE("channels/{channelId}/messages/{messageId}")
    fun deleteMessage(
        @Path("channelId") channelId: String, @Path("messageId") messageId: String?, @Header(
            "Authorization"
        ) token: String
    ): Observable<Response<Unit>>

    data class UpdateMessageRequest(
        @SerializedName("content")
        val content: String
    )

    @PATCH("channels/{channelId}/messages/{messageId}")
    fun updateMessage(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String?,
        @Body request: UpdateMessageRequest,
        @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonObject>

    @PUT("channels/{channelId}/messages/{messageId}/reactions/{identifier}/@me")
    fun addReaction(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String,
        @Path("identifier") identifier: String,
        @Header(
            "Authorization"
        ) token: String
    ): Observable<Response<Unit>>

    @DELETE("channels/{channelId}/messages/{messageId}/reactions/{identifier}/@me")
    fun deleteReaction(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String,
        @Path("identifier") identifier: String,
        @Header(
            "Authorization"
        ) token: String
    ): Observable<Void>

    @POST("channels/{channelId}/messages/{messageId}/ack")
    fun ackMessage(
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String?,
        @Header("Authorization")
        token: String
    ): Observable<Void>

}