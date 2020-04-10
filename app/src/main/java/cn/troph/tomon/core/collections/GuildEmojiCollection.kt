package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.GuildEmoji

class GuildEmojiCollection(client: Client, private val guildId: String) :
    BaseCollection<GuildEmoji>(client) {

    val guild get() = client.guilds.get(guildId)

    // forbid add
    override fun add(
        data: JsonData,
        identify: ((d: JsonData) -> String)?
    ): GuildEmoji? {
        return null
    }
}