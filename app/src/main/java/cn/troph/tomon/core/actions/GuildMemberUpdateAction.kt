package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.events.GuildMemberUpdateEvent
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Role
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.asCollectionOfType
import com.google.gson.JsonElement

class GuildMemberUpdateAction(client: Client) : Action<GuildMember>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): GuildMember? {
        val obj = data!!.asJsonObject
        val guild = client.guilds.get(obj["guild_id"].asString)
        var member: GuildMember?
        if (guild != null) {
            member = guild.members.get((obj["user"].asJsonObject)["id"].asString)
            if (member != null) {
                val oldRoles = member.roles.collection.keys.toSet()
                member.patch(obj)
                val newRoles = member.roles.collection.keys.toSet()
                client.eventBus.postEvent(GuildMemberUpdateEvent(member = member))
                val unionRoles = oldRoles.union(newRoles)
                val interRoles = oldRoles.intersect(newRoles)
                if (unionRoles.size - interRoles.size > 0) {
                    val channels = getAffectedChannels(
                        guild = guild, members = Collection(
                            mapOf<String, GuildMember>(member.id to member)
                        )
                    ).asCollectionOfType<GuildChannel>()!!
                    val inChannels = guild.channels.filter { it.members.has(member!!.id) }
                    channels.merge(inChannels)
                    channels.forEach { client.eventBus.postEvent(ChannelMemberUpdateEvent(channel = it)) }
                }
            }
        } else
            member = null
        return member
    }
}