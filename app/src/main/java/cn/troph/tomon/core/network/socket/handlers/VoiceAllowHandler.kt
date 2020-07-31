package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.VoiceAllowConnectEvent
import cn.troph.tomon.core.events.VoiceLeaveChannelEvent
import cn.troph.tomon.core.events.VoiceStateUpdateEvent
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.structures.VoiceConnectStateReceive
import cn.troph.tomon.core.structures.VoiceUpdate
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger


val handleVoiceStateHandler: Handler = { client, packet ->
    Logger.d(packet)
    client.eventBus.postEvent(
        VoiceStateUpdateEvent(
            Gson().fromJson(
                packet["d"].asJsonObject,
                VoiceUpdate::class.java
            )
        )
    )
}

val handleVoiceLeaveHandler: Handler = { client: Client, packet: JsonObject ->
    Client.global.eventBus.postEvent(
        VoiceLeaveChannelEvent(
            Gson().fromJson(
                packet.asJsonObject["d"].asJsonObject,
                VoiceConnectStateReceive::class.java
            )
        )
    )
}

val handleVoiceJoinHandler: Handler = { client: Client, packet: JsonObject ->
    Client.global.eventBus.postEvent(
        VoiceAllowConnectEvent(
            Gson().fromJson(
                packet.asJsonObject["d"].asJsonObject,
                VoiceConnectStateReceive::class.java
            )
        )
    )
}


