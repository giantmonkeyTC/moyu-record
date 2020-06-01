package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import com.google.gson.JsonElement
import com.google.gson.JsonParser

val handleMessageReactionAdd: Handler = { client: Client, packet: JsonElement ->
    client.actions.reactionAdd(packet.asJsonObject["d"].asJsonObject)
}