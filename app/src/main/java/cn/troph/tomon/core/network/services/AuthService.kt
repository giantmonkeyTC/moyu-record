package cn.troph.tomon.core.network.services

import cn.troph.tomon.core.JsonData
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
    suspend fun login(
        @Body request: LoginRequest
    ): JsonData;

    data class RegisterRequest(
        val username: String?,
        val email: String?,
        val phone: String?,
        val code: String?,
        val password: String?
    )

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): JsonData;

}