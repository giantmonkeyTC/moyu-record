package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildMember

class GuildMemberCollection(val guild: Guild) : BaseCollection<GuildMember>(guild.client) {
    override fun add(
        data: Map<String, Any>,
        identify: ((d: Map<String, Any>) -> String)?
    ): GuildMember? {
        data.toMutableMap()["id"] = (data["user"] as Map<*, *>)["id"] as String
        return super.add(data, identify)
    }

    override fun instantiate(data: Map<String, Any>): GuildMember? {
        return GuildMember(client, data, guild)
    }

}