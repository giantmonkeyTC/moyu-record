package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.events.GuildCreateEvent
import cn.troph.tomon.core.structures.Guild

class GuildCreateAction(client: Client) : Action<Guild>(client) {

    override fun handle(data: Any, extra: Any?): Guild? {
        val obj = data as JsonData
        val existing = client.guilds.has(obj["id"] as String)
        val guild = client.guilds.add(obj)
        if (existing == null && guild != null)
            client.eventBus.postEvent(GuildCreateEvent(guild = guild))
        return guild


    }
}