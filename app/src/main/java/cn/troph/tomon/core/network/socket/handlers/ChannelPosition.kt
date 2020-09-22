package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonNull

val handleChannelPosition: Handler = { client: Client, packet: JsonElement ->
    client.actions.channelPosition(packet.asJsonObject["d"].asJsonObject)
}