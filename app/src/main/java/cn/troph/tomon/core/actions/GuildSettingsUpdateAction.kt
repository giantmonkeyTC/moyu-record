package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildSettingsUpdateEvent
import cn.troph.tomon.core.structures.GuildSettings
import com.google.gson.JsonElement

class GuildSettingsUpdateAction(client: Client) : Action<GuildSettings>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): GuildSettings? {
        val settings = client.guildSettings.add(data!!.asJsonObject) { it["guild_id"].asString }
        if (settings != null) {
            client.eventBus.postEvent(GuildSettingsUpdateEvent(settings))
        }
        return settings
    }

}