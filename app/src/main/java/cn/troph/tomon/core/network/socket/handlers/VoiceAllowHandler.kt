package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.VoiceStateUpdateEvent
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.structures.VoiceUpdate
import com.google.gson.Gson
import com.google.gson.JsonObject


val handleVoiceHandler: Handler = { client: Client, packet: JsonObject ->
    client.actions.joinVoiceChannel(packet.asJsonObject["d"].asJsonObject)
}

val handleVoiceStateHandler: Handler = { client, packet ->
    client.eventBus.postEvent(
        VoiceStateUpdateEvent(
            Gson().fromJson(
                packet["d"].asJsonObject,
                VoiceUpdate::class.java
            )
        )
    )
}


