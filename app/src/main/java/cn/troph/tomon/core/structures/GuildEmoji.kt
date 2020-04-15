package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject

class GuildEmoji(client: Client, data: JsonObject) : Emoji(client, data) {

    var guildId: String? = null
        private set

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("guild_id")) {
            guildId = data["guild_id"].optString
        }
    }

    val guild get(): Guild? = client.guilds.get(guildId ?: "")
}