package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class GuildEmoji(client: Client, data: JsonData) : Emoji(client, data) {
    private val guildId: String? = data["guild_id"] as? String

    val guild get(): Guild? = client.guilds.get(guildId ?: "")
}