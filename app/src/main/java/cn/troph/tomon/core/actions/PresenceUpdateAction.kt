package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.events.PresenceUpdateEvent
import cn.troph.tomon.core.events.UserUpdateEvent
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.Presence
import cn.troph.tomon.core.utils.Collection
import com.google.gson.JsonElement

class PresenceUpdateAction(client: Client) : Action<Presence>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): Presence? {
        val obj = data!!.asJsonObject
        val existUser = client.users[obj["user"].asJsonObject["id"].asString]
        val presence = client.presences.add(obj) { it["user"].asJsonObject["id"].asString }
        if (existUser == null && presence?.user != null) {
            client.eventBus.postEvent(UserUpdateEvent(presence.user!!))
        }
        if (presence != null) {
            client.eventBus.postEvent(PresenceUpdateEvent(presence))
            val guilds = getAffectedGuilds(client.guilds, presence.userId)
            val channels = Collection<Channel>()
            guilds.forEach { guild ->
                val member = guild.members[presence.userId]
                if (member != null) {
                    guild.channels.filter { channel ->
                        channel.members.has(member.id)
                    }.forEach { c ->
                        channels[c.id] = c
                    }
                }
            }
            channels.forEach { channel ->
                client.eventBus.postEvent(ChannelMemberUpdateEvent(channel))
            }
        }
        return presence
    }

}