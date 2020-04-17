package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.events.GuildMemberAddEvent
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.structures.GuildMember
import com.google.gson.JsonElement
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.asCollectionOfType

class GuildMemberAddAction(client: Client) : Action<GuildMember>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): GuildMember? {
        val obj = data!!.asJsonObject
        val guild = client.guilds.get(obj["data_id"].asString)
        var member: GuildMember?
        if (guild != null) {
            val existing = guild.members.has((obj["user"].asJsonObject)["id"].asString)
            member = guild.members.add(obj)
            if (!existing && member != null) {
                client.eventBus.postEvent(GuildMemberAddEvent(member = member))
                // 更新channel member list
                val channels = getAffectedChannels(
                    guild = guild,
                    members = Collection(mapOf<String, GuildMember>(pair = Pair(member.id, member)))
                ).asCollectionOfType<GuildChannel>()!!
                val inChannels = guild.channels.filter { it.members.has(member!!.id) }
                channels.merge(inChannels)
                channels.forEach { client.eventBus.postEvent(ChannelMemberUpdateEvent(it)) }
            }
        } else
            member = null
        return member
    }
}