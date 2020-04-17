package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelCreateEvent
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import com.google.gson.JsonElement

class ChannelCreateAction(client: Client) : Action<Channel>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): Channel? {
        val obj = data!!.asJsonObject
        val existing = client.channels.has(obj["id"].asString)
        val channel = client.channels.add(obj)
        if (existing == null && channel != null) {
            client.eventBus.postEvent(ChannelCreateEvent(channel))
            if (channel is GuildChannel) {
                var changed = false
                channel.guild?.members?.forEach { member ->
                    changed = getChannelMemberChanged(channel, member)
                }
                if (changed) {
                    client.eventBus.postEvent(ChannelMemberUpdateEvent(channel))
                }
            }
        }
        return channel
    }
}