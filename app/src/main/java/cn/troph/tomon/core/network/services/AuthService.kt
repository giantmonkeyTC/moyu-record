package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonData
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    data class LoginRequest(
        val email: String? = null,
        val phone: String? = null,
        val password: String? = null,
        val token: String? = null
    )

    @POST("auth/login")
    fun login(
        @Body request: LoginRequest
    ): Observable<JsonData>;

    data class RegisterRequest(
        val username: String?,
        val email: String?,
        val phone: String?,
        val code: String?,
        val password: String?
    )

    @POST("auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Observable<JsonData>;

}