package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.events.PresenceFetchEvent
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Presence
import cn.troph.tomon.core.utils.Collection
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class PresenceFetchAction(client: Client) : Action<List<Presence>>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): List<Presence>? {
        val guildId = extras[0] as? String
        val presences = mutableListOf<Presence>()
        val parse = { obj: JsonObject ->
            val presence = client.presences.add(obj) { it["user_id"].asString }
            if (presence != null) {
                presences.add(presence)
            }
        }
        if (data!!.isJsonArray) {
            data.asJsonArray.forEach { parse(it.asJsonObject) }
        } else {
            parse(data.asJsonObject)
        }
        if (presences.size > 0) {
            client.eventBus.postEvent(PresenceFetchEvent(presences))
            val guild = if (guildId != null) client.guilds[guildId] else null
            if (guild != null) {
                val members = presences.mapNotNull { presence ->
                    guild.members[presence.userId]
                }
                val collect = Collection<GuildMember>()
                members.forEach { m -> collect[m.id] = m }
                val channels = getAffectedChannels(guild, collect)
                channels.forEach { channel ->
                    client.eventBus.postEvent(ChannelMemberUpdateEvent(channel))
                }
            }
        }
        return presences
    }
}