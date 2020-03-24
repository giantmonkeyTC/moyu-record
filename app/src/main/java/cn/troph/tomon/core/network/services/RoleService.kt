package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import retrofit2.Call
import retrofit2.http.*

interface RoleService {
    @GET("guilds/{guildId}/roles")
    fun getRoles(@Path("guildId") guildId: String, @Header("Authorization") token: String): Call<JsonArray>;

    data class CreateRoleRequest(
        val name: String,
        val color: Int?,
        val permissions: Int?
    )

    @POST("guilds/{guildId}/roles/{id}")
    fun createRole(
        @Path("guildId") guildId: String,
        @Body request: CreateRoleRequest,
        @Header("Authorization") token: String
    ): Call<JsonData>

    data class UpdateRoleRequest(
        val name: String?,
        val color: Int?,
        val hoist: Boolean?,
        val permissions: Int?
    )

    @PATCH("guilds/{guildId}/roles/{roleId}")
    fun updateRole(
        @Path("guildId") guildId: String, @Path("roleId") roleId: String, @Body request: UpdateRoleRequest, @Header(
            "Authorization"
        ) token: String
    ): Call<JsonData>

    data class UpdatePositionsRequest(
        val positions: Int
    )

    @PATCH("guilds/{guildId}/roles")
    fun updatePositions(
        @Path("guildId") guildId: String, @Body request: UpdatePositionsRequest, @Header(
            "Authorization"
        ) token: String
    ): Call<JsonData>

    @DELETE("guilds/{guildId}/roles/{roleId}")
    fun deleteRole(
        @Path("guildId") guildId: String, @Path("roleId") roleId: String, @Header(
            "Authorization"
        ) token: String
    ): Call<Void>
}