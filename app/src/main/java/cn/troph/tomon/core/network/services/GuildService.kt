package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import retrofit2.http.*

interface GuildService {

    @GET("users/@me/guilds")
    suspend fun getGuilds(@Header("Authorization") token: String): JsonArray;

    @GET("guilds/{id}")
    suspend fun getGuild(@Path("id") id: String, @Header("Authorization") token: String?): JsonData;

    data class CreateGuildRequest(
        val name: String,
        val icon: String
    )


    @POST("guilds")
    suspend fun createGuild(
        @Body request: CreateGuildRequest,
        @Header("Authorization") token: String
    ): JsonData

    data class ModifyGuildRequest(
        val name: String?,
        val icon: String?
    )

    @PATCH("guilds/{id}")
    suspend fun modifyGuild(
        @Path("id") id: String,
        @Body request: ModifyGuildRequest,
        @Header("Authorization") token: String
    ): JsonData

    @DELETE("guilds/{id}")
    suspend fun deleteGuild(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Void

    @DELETE("users/@me/guilds/{id}")
    suspend fun leaveGuild(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Void


}