package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.GuildEmoji
import com.google.gson.JsonObject

class GuildEmojiCollection(client: Client, private val guildId: String) :
    BaseCollection<GuildEmoji>(client) {

    val guild get() = client.guilds.get(guildId)

    // forbid add
    override fun add(
        data: JsonObject,
        identify: ((d: JsonObject) -> String)?
    ): GuildEmoji? {
        return null
    }
}