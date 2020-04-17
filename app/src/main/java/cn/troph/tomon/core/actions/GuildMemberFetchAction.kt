package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.events.GuildMemberFetchEvent
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class GuildMemberFetchAction(client: Client) : Action<List<GuildMember>>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): List<GuildMember>? {
        val isSync: Boolean = (extras[0] as? Boolean) ?: true
        val getGuildId = { data: JsonElement ->
            if (data.isJsonArray) {
                data.asJsonArray.get(0)?.asJsonObject?.get("guild_id")?.optString
            } else {
                data.asJsonObject["guild_id"].optString
            }
        }
        val guildId: String =
            extras[1] as? String ?: (getGuildId(data!!) ?: "")
        val guild = (if (guildId == "") null else client.guilds.get(guildId)) ?: return null
        // 如果同步，移除没有出现过的member
        val removed = mutableListOf<GuildMember>()
        if (isSync) {
            val keys = guild.members.keys.asSequence().toMutableSet()
            val array = data!!.asJsonArray
            array.forEach {
                keys.remove(it.asJsonObject["user"].asJsonObject["id"].asString)
            }
            keys.forEach {
                val member = guild.members.remove(it)
                if (member != null) removed.add(member)
            }
        }
        val members = mutableListOf<GuildMember>()
        val parse = { obj: JsonObject ->
            val role = guild.members.add(obj)
            if (role != null) {
                members.add(role)
            }
        }
        if (data!!.isJsonArray) {
            data.asJsonArray.forEach { parse(it.asJsonObject) }
        } else {
            parse(data.asJsonObject)
        }
        if (members.size > 0) {
            client.eventBus.postEvent(GuildMemberFetchEvent(members))
            // channel member lists
            val removeCollect = Collection<GuildMember>()
            removed.forEach { m -> removeCollect[m.id] = m }
            val removeChannels = getAffectedChannelsForRemoveMembers(guild, removeCollect)
            val collect = Collection<GuildMember>()
            members.forEach { m -> collect[m.id] = m }
            val channels = getAffectedChannels(guild, collect)
            channels.merge(removeChannels)
            channels.forEach { channel ->
                client.eventBus.postEvent(ChannelMemberUpdateEvent(channel))
            }
        }
        return members
    }

}