package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.optInt
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
            bitrate = data["bitrate"].optInt ?: 36800
        }
        if (data.has("user_limit")) {
            userLimit = data["user_limit"].optInt ?: 0
        }
        guild?.let {
            isJoined = false
            it.voiceStates.forEach { vu ->
                if (vu.channelId == id) {
                    isJoined = true
                    voiceStates.add(vu)
                }
            }

        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }
}