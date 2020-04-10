package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.GuildMember

class GuildMemberCollection(client: Client, private val guildId: String) :
    BaseCollection<GuildMember>(client) {

    val guild get() = client.guilds.get(guildId)

    override fun add(
        data: JsonData,
        identify: ((d: JsonData) -> String)?
    ): GuildMember? {
        val id = (data["user"] as JsonData)["id"] as String
        return super.add(data, identify ?: { _ -> id })
    }

    override fun instantiate(data: JsonData): GuildMember? {
        return GuildMember(client, data)
    }

}