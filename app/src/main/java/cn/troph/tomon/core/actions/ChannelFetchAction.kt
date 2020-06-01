package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelFetchEvent
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.events.ChannelSyncEvent
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ChannelFetchAction(client: Client) : Action<List<Channel>>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): List<Channel>? {
        val isSync: Boolean = (extras[0] as? Boolean) ?: true
        val getGuildId = { el: JsonElement ->
            if (el.isJsonArray) {
                if (el.asJsonArray.size() > 0) {
                    el.asJsonArray.get(0)?.asJsonObject?.get("guild_id")?.optString
                } else {
                    ""
                }
            } else {
                el.asJsonObject["guild_id"].optString
            }
        }
        // DM频道使用@me作为guildId
        val guildId: String =
            extras[1] as? String ?: (getGuildId(data!!) ?: "@me")
        val guild = if (guildId == "@me") null else client.guilds.get(guildId)
        // 删除没有的
        if (isSync) {
            val array = data!!.asJsonArray
            val collect =
                if (guildId == "@me") client.dmChannels.clone() else guild?.channels?.clone()
            if (collect != null) {
                array.forEach {
                    val d = it.asJsonObject
                    collect.remove(d["id"].asString)
                }
                collect.forEach {
                    client.channels.remove(it.id)
                }
            }
        }
        val channels = mutableListOf<Channel>()
        val updateChannels = mutableListOf<Channel>()
        val parse = { obj: JsonObject ->
            val exist = client.channels[obj["id"].asString]
            val channel = client.channels.add(obj)
            if (exist == null) {
                if (channel is GuildChannel) {
                    var changed = false
                    channel.members.forEach { member ->
                        changed = getChannelMemberChanged(channel, member)
                    }
                    if (changed) {
                        updateChannels.add(channel)
                    }
                }
            }
            if (channel != null) {
                channels.add(channel)
            }
        }
        if (data!!.isJsonArray) {
            data.asJsonArray.forEach { parse(it.asJsonObject) }
        } else {
            parse(data.asJsonObject)
        }
        if (isSync) {
            client.eventBus.postEvent(ChannelSyncEvent(guild))
        } else {
            client.eventBus.postEvent(ChannelFetchEvent(channels))
        }
        // 成员更新
        updateChannels.forEach { channel ->
            client.eventBus.postEvent(ChannelMemberUpdateEvent(channel))
        }
        return channels
    }
}