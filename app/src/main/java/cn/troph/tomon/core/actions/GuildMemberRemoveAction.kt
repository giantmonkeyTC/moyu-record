package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.events.GuildMemberRemoveEvent
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.utils.Collection
import com.google.gson.JsonElement

class GuildMemberRemoveAction(client: Client) : Action<GuildMember>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): GuildMember? {
        val obj = data!!.asJsonObject
        val guild = client.guilds.get(obj["guild_id"].asString)
        var member: GuildMember?
        if (guild != null) {
            member = guild.members.get((obj["user"].asJsonObject)["id"].asString)
            if (member != null) {
                guild.members.remove((obj["user"].asJsonObject)["id"].asString)
                // 更新所有拥有此member overwrites的channel
                guild.channels.forEach { it.deletePermissionOverwritesLocal(member!!.id) }
                // 更新所有channel的member list
                val channels = getAffectedChannelsForRemoveMembers(
                    guild = guild, members = Collection<GuildMember>(
                        mapOf(member.id to member)
                    )
                )
                channels.forEach { client.eventBus.postEvent(ChannelMemberUpdateEvent(channel = it)) }
                client.eventBus.postEvent(GuildMemberRemoveEvent(member = member))
            }
        } else
            member = null
        return member
    }
}