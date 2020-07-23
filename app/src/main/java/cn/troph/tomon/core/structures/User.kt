package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.ChannelService
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

open class User(client: Client, data: JsonObject) : Base(client, data) {
    var id: String = ""
        protected set
    var username: String = ""
        protected set
    var discriminator: String = ""
        protected set
    var name: String = ""
        protected set
    var avatar: String? = null
        protected set
    var avatarURL: String? = null
        protected set

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("username")) {
            username = data["username"].asString
        }
        if (data.has("discriminator")) {
            discriminator = data["discriminator"].asString
        }
        if (data.has("name")) {
            name = data["name"].asString
        }
        if (data.has("avatar")) {
            avatar = data["avatar"].optString
            if (avatar?.isEmpty() != false) {
                avatar = null
            }
        }
        if (data.has("avatar_url")) {
            avatarURL = data["avatar_url"].optString
            if (avatarURL?.isEmpty() != false) {
                avatarURL = null
            }
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    val identifier: String get() = "$username#$discriminator"

    fun directMessage(userId: String): Observable<JsonObject> {
        return client.rest.channelService.createDmChannel(
            client.auth,
            request = ChannelService.CreateDmChannelRequest(mutableListOf(userId))
        ).doOnError { error -> println(error) }.subscribeOn(
            Schedulers.io()
        ).map {
            it
        }
    }
}