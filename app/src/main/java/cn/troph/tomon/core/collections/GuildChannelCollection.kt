package cn.troph.tomon.core.collections

import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildChannel

class GuildChannelCollection(val guild: Guild) : BaseCollection<GuildChannel>(guild.client) {

    // forbid add
    override fun add(
        data: JsonData,
        identify: ((d: JsonData) -> String)?
    ): GuildChannel? {
        return null
    }

}