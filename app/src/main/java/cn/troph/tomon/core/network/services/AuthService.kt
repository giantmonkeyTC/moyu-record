package cn.troph.tomon.core.network.services

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface AuthService {

    data class LoginRequest(
        @SerializedName("full_name")
        val full_name: String? = null,
        @SerializedName("email")
        val email: String? = null,
        @SerializedName("phone")
        val phone: String? = null,
        @SerializedName("password")
        val password: String? = null,
        @SerializedName("token")
        val token: String? = null
    )

    @DELETE("users/@me")
    fun deleteAccount()

    @POST("auth/login")
    fun login(
        @Body request: LoginRequest
    ): Observable<JsonObject>

    data class RegisterRequest(
        @SerializedName("username")
        val username: String?,
        @SerializedName("email")
        val email: String?,
        @SerializedName("phone")
        val phone: String?,
        @SerializedName("code")
        val code: String?,
        @SerializedName("password")
        val password: String?,
        @SerializedName("invite")
        val invite: String?,
        @SerializedName("force") var force: Boolean = true
    )

    @POST("auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Observable<JsonObject>

    data class VerifyRequest(
        @SerializedName("phone")
        val phone: String?,
        @SerializedName("type")
        val type: String?
    )

    @POST("auth/verification")
    fun verify(
        @Body request: VerifyRequest
    ): Observable<Response<Void>>

}