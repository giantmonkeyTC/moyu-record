package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelCreateEvent
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.events.DmChannelCreateEvent
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import com.google.gson.JsonElement

class DmChannelCreateAction(client: Client) : Action<Channel>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): Channel? {
        val obj = data!!.asJsonObject
        val existing = client.dmChannels.has(obj["id"].asString)
        val channel = client.dmChannels.add(obj)
        if (!existing && channel != null) {
            client.eventBus.postEvent(DmChannelCreateEvent(channel))
        }
        return channel
    }
}