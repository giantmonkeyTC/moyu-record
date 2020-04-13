package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildUpdateEvent
import cn.troph.tomon.core.structures.Guild
import com.google.gson.JsonElement

class GuildUpdateAction(client: Client) : Action<Guild>(client) {
    override fun handle(data: JsonElement?, extra: Any?): Guild? {
        val obj = data!!.asJsonObject
        val guild = client.guilds.get(obj["id"].asString)
        if (guild != null) {
            guild.update(obj)
            client.eventBus.postEvent(GuildUpdateEvent(guild))
        }
        return guild
    }
}