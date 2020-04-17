package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.GuildSettings
import com.google.gson.Gson
import com.google.gson.JsonObject

class GuildSettingsCollection(client: Client) : BaseCollection<GuildSettings>(client) {

    override fun instantiate(data: JsonObject): GuildSettings? {
        return GuildSettings(client, data)
    }

    override fun get(key: String): GuildSettings? {
        val settings = super.get(key)
        if (settings == null) {
            val guild = client.guilds[key]
            if (guild != null) {
                val newSettings =
                    GuildSettings(client, Gson().toJsonTree(mapOf("guild_id" to key)).asJsonObject)
                set(key, newSettings)
                return newSettings
            }
        }
        return settings
    }

}