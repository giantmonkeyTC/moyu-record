package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.events.GuildDeleteEvent
import cn.troph.tomon.core.structures.Guild

class GuildDeleteAction(client: Client) : Action<Guild>(client) {
    override fun handle(data: Any, extra: Any?): Guild? {
        val obj = data as JsonData
        val guild = client.guilds.get(obj["id"] as String)
        if (guild != null) {
            for (channel in guild.channels.values)
                client.channels.remove(channel.id)
            client.guilds.remove(guild.id)
            client.eventBus.postEvent(GuildDeleteEvent(guild = guild))
        }
        return guild
    }

}