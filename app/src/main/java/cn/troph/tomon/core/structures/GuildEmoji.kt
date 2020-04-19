package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject

class GuildEmoji(client: Client, data: JsonObject) : Emoji(client, data) {

    var guildId: String? = null
        private set

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("guild_id")) {
            guildId = data["guild_id"].optString
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    val guild get(): Guild? = client.guilds[guildId ?: ""]
}