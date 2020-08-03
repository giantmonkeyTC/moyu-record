package cn.troph.tomon.core.network.services

import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface GuildEmojiService {

    @GET("stamppacks/{packId}")
    fun fetchStampPack(
        @Path("packId") packId: String, @Header(
            "Authorization"
        ) token: String
    ): Observable<JsonObject>

}