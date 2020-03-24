package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    data class LoginRequest(
        val email: String?,
        val phone: String?,
        val password: String?,
        val token: String?
    )

    @POST("auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<JsonData>;

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
    ): Call<JsonData>;

}