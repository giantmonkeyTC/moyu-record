package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.ChannelService
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

open class User(client: Client, data: JsonObject) : Base(client, data) {
    @Expose
    var id: String = ""
        protected set

    @Expose
    var username: String = ""
        protected set

    @Expose
    var discriminator: String = ""
        protected set

    @Expose
    var name: String = ""
        protected set

    @Expose
    var avatar: String? = null
        protected set

    @Expose
    var avatarURL: String? = null
        protected set

    @Expose
    var isSpeaking = false

    @Expose
    var isSelfMute = false

    @Expose
    var isSelfDeaf = false

    @Expose
    var type = 0

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("type")) {
            type = data["type"].asInt
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