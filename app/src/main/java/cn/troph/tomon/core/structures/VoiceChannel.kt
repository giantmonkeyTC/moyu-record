package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

class VoiceChannel(client: Client, data: JsonObject) :
    GuildChannel(client, data) {

    var bitrate: Int = 36800
        private set
    var userLimit: Int = 0
        private set

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("bitrate")) {
            bitrate = data["bitrate"].asInt
        }
        if (data.has("user_limit")) {
            userLimit = data["user_limit"].asInt
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }
}