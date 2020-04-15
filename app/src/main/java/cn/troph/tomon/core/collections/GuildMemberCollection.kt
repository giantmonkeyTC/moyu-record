package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildMember
import com.google.gson.JsonObject

class GuildMemberCollection(client: Client, val guild: Guild) :
    BaseCollection<GuildMember>(client) {

    override fun add(
        data: JsonObject,
        identify: ((d: JsonObject) -> String)?
    ): GuildMember? {
        val id = (data["user"] as JsonObject)["id"] as String
        return super.add(data, identify ?: { _ -> id })
    }

    override fun instantiate(data: JsonObject): GuildMember? {
        return GuildMember(client, data)
    }

}