package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface GuildService {

    @GET("users/@me/guilds")
    fun getGuilds(@Header("Authorization") token: String): Observable<JsonArray>;

    @GET("guilds/{id}")
    suspend fun getGuild(@Path("id") id: String, @Header("Authorization") token: String?): JsonData;

    data class CreateGuildRequest(
        val name: String,
        val icon: String
    )


    @POST("guilds")
    fun createGuild(
        @Body request: CreateGuildRequest,
        @Header("Authorization") token: String
    ): Observable<JsonData>

    data class ModifyGuildRequest(
        val name: String?,
        val icon: String?
    )

    @PATCH("guilds/{id}")
    fun modifyGuild(
        @Path("id") id: String,
        @Body request: ModifyGuildRequest,
        @Header("Authorization") token: String
    ): Observable<JsonData>

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