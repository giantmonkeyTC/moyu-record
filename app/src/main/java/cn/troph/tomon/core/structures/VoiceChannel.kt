package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

class VoiceChannel(client: Client, data: JsonObject) :
    GuildChannel(client, data) {

    var bitrate: Int = 36800
        private set
    var userLimit: Int = 0
        private set

    val voiceStates = mutableListOf<VoiceUpdate>()

    var isJoined = false

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
        guild?.let {
            it.voiceStates.forEach {
                if (it.channelId == id) {
                    voiceStates.add(it)
                }
            }
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }
}