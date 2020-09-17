package cn.troph.tomon.core.network.services

import cn.troph.tomon.ui.chat.fragments.*
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface InviteService {
    @POST("invites/{code}")
    fun join(
        @Path("code") code: String,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    @GET("invites/{code}")
    fun fetch(
        @Path("code") code: String,
        @Header("Authorization") token: String
    ): Observable<Invite>

    @POST("channels/{channelId}/invites")
    fun getChannelInvite(
        @Path("channelId") channelId: String,
        @Header("Authorization") token: String
    ): Observable<ChannelInvite>

    @POST("users/@me/tickets")
    fun getTickets(
        @Header("Authorization") token: String
    ):Observable<Ticket>

    data class Ticket(
        @SerializedName("code")
        val code:String,
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("users")
        val users: MutableList<TicketUser>,
        @SerializedName("uses")
        val uses: Int,
        @SerializedName("max_uses")
        val maxUses: Int
    )
    data class TicketUser(
        @SerializedName("id")
        val id: String,
        @SerializedName("username")
        val username: String,
        @SerializedName("discriminator")
        val discriminator: String,
        @SerializedName("avatar")
        val avatar: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("avatar_url")
        val avatar_url: String
    )
}