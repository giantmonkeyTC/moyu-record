package cn.troph.tomon.core.network.services

import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    data class LoginRequest(
        val full_name: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val password: String? = null,
        val token: String? = null
    )

    @POST("auth/login")
    fun login(
        @Body request: LoginRequest
    ): Observable<JsonObject>

    data class RegisterRequest(
        val username: String?,
        val email: String?,
        val phone: String?,
        val code: String?,
        val password: String?,
        val invite: String?
    )

    @POST("auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Observable<JsonObject>

    data class VerifyRequest(
        val phone: String?,
        val type: String?
    )

    @POST("auth/verification")
    fun verify(
        @Body request: VerifyRequest
    ): Observable<Response<Void>>

}