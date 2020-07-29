package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.VoiceStateUpdateEvent
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.structures.VoiceUpdate
import com.google.gson.Gson
import com.google.gson.JsonObject

val handleVoiceConnectAllow: Handler = { client: Client, packet: JsonObject ->
    if (packet.has("e")) {
        when (packet["e"].asString) {
            "VOICE_STATE_UPDATE" -> {
                client.eventBus.postEvent(
                    VoiceStateUpdateEvent(
                        Gson().fromJson(
                            packet,
                            VoiceUpdate::class.java
                        )
                    )
                )
            }
        }
    } else {
        client.actions.joinVoiceChannel(packet.asJsonObject["d"].asJsonObject)
    }

}

