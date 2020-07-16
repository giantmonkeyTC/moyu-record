package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import com.google.gson.JsonElement

val handlePresenceUpdate: Handler = { client: Client, packet: JsonElement ->
    client.actions.presenceUpdate(packet.asJsonObject["d"].asJsonObject)
}