package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonNull

val handleIdentity: Handler = { client: Client, packet: JsonElement ->
    val data = packet.asJsonObject["d"].asJsonObject
    data["guilds"].asJsonArray.forEach { ele ->
        val guild = ele.asJsonObject
        if (guild["system_channel_id"].optString == "0") {
            guild.add("system_channel_id", JsonNull.INSTANCE)
        }
    }
    data["dm_channels"].asJsonArray.forEach { ele ->
        val channel = ele.asJsonObject
        if (channel["guild_id"].optString == "0") {
            channel.addProperty("guild_id", "@me")
        }
    }
    client.actions.guildFetch(data["guilds"].asJsonArray, true)
    client.actions.channelFetch(data["dm_channels"].asJsonArray, true)
    data["guilds"].asJsonArray.forEach {
        val guild = it.asJsonObject
        client.actions.channelFetch(guild["channels"].asJsonArray, true, guild["id"].asString)
        client.actions.roleFetch(guild["roles"].asJsonArray, true, guild["id"].asString)
        client.actions.emojiFetch(guild["emojis"].asJsonArray, true, guild["id"].asString)
    }
}