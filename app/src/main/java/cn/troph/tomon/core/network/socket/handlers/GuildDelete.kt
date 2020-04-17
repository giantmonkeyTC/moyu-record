package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonNull

val handleGuildDelete: Handler = { client: Client, packet: JsonElement ->
    client.actions.guildDelete(packet.asJsonObject["d"].asJsonObject)
}