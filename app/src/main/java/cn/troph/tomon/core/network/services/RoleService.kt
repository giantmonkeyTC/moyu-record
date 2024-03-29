package cn.troph.tomon.core.network.services

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface RoleService {
    @GET("guilds/{guildId}/roles")
    fun getRoles(
        @Path("guildId") guildId: String,
        @Header("Authorization") token: String
    ): Observable<JsonArray>;

    data class CreateRoleRequest(
        @SerializedName("name")
        val name: String,
        @SerializedName("color")
        val color: Int?,
        @SerializedName("permission")
        val permissions: Int?
    )

    @POST("guilds/{guildId}/roles/{id}")
    fun createRole(
        @Path("guildId") guildId: String,
        @Body request: CreateRoleRequest,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    data class UpdateRoleRequest(
        @SerializedName("name")
        val name: String?,
        @SerializedName("color")
        val color: Int?,
        @SerializedName("hoist")
        val hoist: Boolean?,
        @SerializedName("permission")
        val permissions: Int?
    )

    @PATCH("guilds/{guildId}/roles/{roleId}")
    fun updateRole(
        @Path("guildId") guildId: String,
        @Path("roleId") roleId: String,
        @Body request: UpdateRoleRequest,
        @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonObject>


    data class UpdatePositionsRequest(
        @SerializedName("positions")
        val positions: Int
    )

    @PATCH("guilds/{guildId}/roles")
    fun updatePositions(
        @Path("guildId") guildId: String, @Body request: UpdatePositionsRequest, @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonObject>

    @DELETE("guilds/{guildId}/roles/{roleId}")
    fun deleteRole(
        @Path("guildId") guildId: String, @Path("roleId") roleId: String, @Header(
            "Authorization"
        ) token: String
    ): Observable<Void>
}