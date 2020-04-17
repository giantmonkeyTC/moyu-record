package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonNull

val handleGuildUpdate: Handler = { client: Client, packet: JsonElement ->
    val g = packet.asJsonObject["d"].asJsonObject
    if (g["system_channel_id"].optString == "0") {
        g.add("system_channel_id", JsonNull.INSTANCE)
    }
    client.actions.guildUpdate(g)
}