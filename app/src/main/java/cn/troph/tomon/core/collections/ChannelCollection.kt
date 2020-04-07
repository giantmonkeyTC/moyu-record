package cn.troph.tomon.core.collections

import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.*


class ChannelCollection(client: Client, m: Map<String, Channel>? = null) :
    BaseCollection<Channel>(client, m) {
    override fun add(
        data: JsonData,
        identify: ((d: JsonData) -> String)?
    ): Channel? {
        val channel = super.add(data, identify)
        if (channel is GuildChannel) {
            channel.guild?.channels?.put(channel.id, channel)
        }
        return channel
    }

    override fun remove(key: String): Channel? {
        val channel = get(key)
        if (channel is GuildChannel)
            channel.guild?.channels?.remove(key)
        return super.remove(key)
    }

    override fun instantiate(data: JsonData): Channel? {
        var guild = client.guilds.get(data["guild_id"] as String)
        when (Channel.typeOf(data["type"] as Int)) {
            ChannelType.TEXT -> {
                if (guild != null) {
                    return TextChannel(client, data, guild)
                }
            }
            ChannelType.VOICE -> {
                if (guild != null) {
                    return VoiceChannel(client, data, guild)
                }
            }
            ChannelType.CATEGORY -> {
                if (guild != null) {
                    return CategoryChannel(client, data, guild)
                }
            }
            else -> return null
        }
        return null
    }

}