package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildDeleteEvent
import cn.troph.tomon.core.structures.Guild
import com.google.gson.JsonElement

class GuildDeleteAction(client: Client) : Action<Guild>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): Guild? {
        val obj = data!!.asJsonObject
        val guild = client.guilds.get(obj["id"].asString)
        if (guild != null) {
            guild.channels.forEach { channel ->
                client.channels.remove(channel.id)
            }
            client.guilds.remove(guild.id)
            client.eventBus.postEvent(GuildDeleteEvent(guild = guild))
        }
        return guild
    }

}