package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildCreateEvent
import cn.troph.tomon.core.structures.Guild
import com.google.gson.JsonElement

class GuildCreateAction(client: Client) : Action<Guild>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): Guild? {
        val obj = data!!.asJsonObject
        val existing = client.guilds.has(obj["id"].asString)
        val guild = client.guilds.add(obj)
        if (existing == null && guild != null) {
            client.eventBus.postEvent(GuildCreateEvent(guild))
        }
        return guild
    }
}