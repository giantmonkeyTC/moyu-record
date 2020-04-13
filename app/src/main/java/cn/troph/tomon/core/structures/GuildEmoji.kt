package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

class GuildEmoji(client: Client, data: JsonObject) : Emoji(client, data) {
    private val guildId: String? = data["guild_id"] as? String

    val guild get(): Guild? = client.guilds.get(guildId ?: "")
}