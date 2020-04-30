package cn.troph.tomon.core.collections

import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.*
import com.google.gson.JsonObject

class ChannelCollection(client: Client) :
    BaseCollection<Channel>(client) {

    override fun add(
        data: JsonObject,
        identify: CollectionIdentify?
    ): Channel? {
        val channel = super.add(data, identify)
        if (channel is GuildChannel) {
            channel.guild?.channels?.put(channel.id, channel)
        } else if (channel is DmChannel) {
            client.dmChannels.put(channel.id, channel)
        }
        return channel
    }

    override fun remove(key: String): Channel? {
        val channel = get(key)
        if (channel is GuildChannel) {
            channel.guild?.channels?.remove(key)
        } else if (channel is DmChannel) {
            client.dmChannels.remove(channel.id)
        }
        return super.remove(key)
    }

    override fun instantiate(data: JsonObject): Channel? {
        val typeValue = data["type"].asInt
        return when (ChannelType.fromInt(typeValue)) {
            ChannelType.TEXT -> TextChannel(client, data)
            ChannelType.VOICE -> VoiceChannel(client, data)
            ChannelType.CATEGORY -> CategoryChannel(client, data)
            ChannelType.DM -> DmChannel(client, data)
            else -> null
        }
    }

}