package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import retrofit2.Call
import retrofit2.http.*

interface GuildService {

    @GET("users/@me/guilds")
    fun getGuilds(@Header("Authorization") token: String): Call<JsonArray>;

    @GET("guilds/{id}")
    fun getGuild(@Path("id") id: String, @Header("Authorization") token: String?): Call<JsonData>;

    data class CreateGuildRequest(
        val name: String,
        val icon: String
    )

    @POST("guilds")
    fun createGuild(
        @Body request: CreateGuildRequest,
        @Header("Authorization") token: String
    ): Call<JsonData>

    data class ModifyGuildRequest(
        val name: String?,
        val icon: String?
    )

    @PATCH("guilds/{id}")
    fun modifyGuild(
        @Path("id") id: String,
        @Body request: ModifyGuildRequest,
        @Header("Authorization") token: String
    ): Call<JsonData>

    @DELETE("guilds/{id}")
    fun deleteGuild(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Call<Void>

    @DELETE("users/@me/guilds/{id}")
    fun leaveGuild(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Call<Void>

}