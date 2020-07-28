package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import com.google.gson.JsonObject

val handleVoiceConnectAllow: Handler = { client: Client, packet: JsonObject ->
    client.actions.joinVoiceChannel(packet.asJsonObject["d"].asJsonObject)
}

