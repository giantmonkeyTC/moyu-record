package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.events.GuildUpdateEvent
import cn.troph.tomon.core.structures.Guild

class GuildUpdateAction(client: Client) : Action<Guild>(client) {
    override fun handle(data: Any, extra: Any?): Guild? {
        val guild: Guild? = client.guilds.get((data as JsonData)["id"] as String)
        if (guild != null) {
            guild.patch(data)
            client.eventBus.postEvent(GuildUpdateEvent(guild))
        }
        return guild
    }
}