package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildSettingsUpdateEvent
import cn.troph.tomon.core.structures.GuildSettings
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement

class GuildSettingsUpdateAction(client: Client) : Action<GuildSettings>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): GuildSettings? {
        val settings = client.guildSettings.add(data!!.asJsonObject) { it["guild_id"].optString ?: "@me" }
        if (settings != null) {
            client.eventBus.postEvent(GuildSettingsUpdateEvent(settings))
        }
        return settings
    }

}