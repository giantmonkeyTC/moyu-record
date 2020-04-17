package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonNull

val handleGuildCreate: Handler = { client: Client, packet: JsonElement ->
    val g = packet.asJsonObject["d"].asJsonObject
    if (g["system_channel_id"].optString == "0") {
        g.add("system_channel_id", JsonNull.INSTANCE)
    }
    val guild = client.actions.guildCreate(g)
    if (g.has("channels")) {
        client.actions.channelFetch(g["channels"], true, guild!!.id)
    }
    if (g.has("roles")) {
        client.actions.roleFetch(g["roles"], true, guild!!.id)
    }
    if (g.has("emojis")) {
        client.actions.emojiFetch(g["emojis"], true, guild!!.id)
    }
    if (g.has("members")) {
        client.actions.guildMemberFetch(g["members"], true, guild!!.id)
    }
}