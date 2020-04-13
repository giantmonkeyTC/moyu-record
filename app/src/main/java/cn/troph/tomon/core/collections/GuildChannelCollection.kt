package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.GuildChannel
import com.google.gson.JsonObject

class GuildChannelCollection(client: Client, private val guildId: String) :
    BaseCollection<GuildChannel>(client) {

    val guild get() = client.guilds.get(guildId)

    // forbid add
    override fun add(
        data: JsonObject,
        identify: ((d: JsonObject) -> String)?
    ): GuildChannel? {
        return null
    }

}