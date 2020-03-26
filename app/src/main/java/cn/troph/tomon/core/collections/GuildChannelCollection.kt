package cn.troph.tomon.core.collections

import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildChannel

class GuildChannelCollection(val guild: Guild) : BaseCollection<GuildChannel>(guild.client) {
    override fun add(
        data: Map<String, Any>,
        identify: ((d: Map<String, Any>) -> String)?
    ): GuildChannel? {
        return null
    }

    //TODO fetch,create,etc.

    override fun instantiate(data: Map<String, Any>): GuildChannel? {
        val guild = client.guilds.get(data["guild_id"] as String)
        return GuildChannel(client, data, guild)
    }
}