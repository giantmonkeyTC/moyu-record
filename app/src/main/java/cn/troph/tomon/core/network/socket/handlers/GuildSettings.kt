package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import com.google.gson.JsonElement

val handleGuildSettings: Handler = { client: Client, packet: JsonElement ->
    client.actions.guildSettingsUpdate(packet.asJsonObject["d"].asJsonObject)
}