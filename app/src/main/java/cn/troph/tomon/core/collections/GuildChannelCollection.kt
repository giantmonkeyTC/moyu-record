package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.GuildChannel

class GuildChannelCollection(client: Client, private val guildId: String) :
    BaseCollection<GuildChannel>(client) {

    val guild get() = client.guilds.get(guildId)

    // forbid add
    override fun add(
        data: JsonData,
        identify: ((d: JsonData) -> String)?
    ): GuildChannel? {
        return null
    }

}