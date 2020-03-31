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

    override fun resolve(member: Any): Any? {
        val memberResolvable = super.resolve(member)
        if(memberResolvable != null)
            return memberResolvable
        val userResolvable = client.users.resolveId(member){it.id}
        if (userResolvable!= null)
            return super.resolve(userResolvable)
        return null
    }

    override fun resolveId(member: Any, id: (value: GuildMember) -> String?): String? {
        val memberResolvable =super.resolveId(member){it.id}
        if (memberResolvable!=null)
            return memberResolvable
        val userResolvable = client.users.resolveId(member){it.id}
        return if(this.has(userResolvable!!)) userResolvable else null
    }

    override fun instantiate(data: Map<String, Any>): GuildMember? {
        return GuildMember(client, data, guild)
    }

}