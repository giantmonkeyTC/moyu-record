package cn.troph.tomon.core.network.services

import com.google.gson.JsonArray
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface GuildMemberService {
    @GET("channels/{channelId}/members")
    fun getMembers(
        @Path("channelId") channelId: String,
        @Header("Authorization") token: String
    ): Observable<JsonArray>
}