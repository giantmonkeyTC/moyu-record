package cn.troph.tomon.core.network.services

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Observable
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

    @DELETE("guilds/{id}")
    fun deleteGuild(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<Void>

    @DELETE("users/@me/guilds/{id}")
    fun leaveGuild(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Observable<Void>


}