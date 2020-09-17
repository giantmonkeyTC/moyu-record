package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import com.google.gson.JsonElement

val handleGuildMemberUpdate: Handler = { client: Client, packet: JsonElement ->
    client.actions.guildMemberUpdate(packet.asJsonObject["d"].asJsonObject)
}