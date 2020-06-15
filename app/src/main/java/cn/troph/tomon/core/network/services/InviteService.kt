package cn.troph.tomon.core.network.services

import cn.troph.tomon.ui.chat.fragments.Invite
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface InviteService {
    @POST("invites/{code}")
    fun join(
        @Path("code") code: String,
        @Header("Authorization") token: String
    ): Observable<JsonObject>

    @GET("invites/{code}")
    fun fetch(
        @Path("code") code: String,
        @Header("Authorization") token: String
    ): Observable<Invite>
}