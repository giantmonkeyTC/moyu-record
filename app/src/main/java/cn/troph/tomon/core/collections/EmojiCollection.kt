package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Emoji
import cn.troph.tomon.core.structures.GuildEmoji
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject

class EmojiCollection(client: Client, m: Map<String, Emoji>? = null) :
    BaseCollection<Emoji>(client, m) {

    override fun add(data: JsonObject, identify: ((d: JsonObject) -> String)?): Emoji? {
        val emoji = super.add(data, identify)
        if (emoji is GuildEmoji) {
            emoji.guild?.emojis?.put(emoji.id, emoji)
        }
        return emoji
    }

    override fun remove(key: String): Emoji? {
        val emoji = get(key)
        if (emoji is GuildEmoji) {
            emoji.guild?.emojis?.remove(key)
        }
        return super.remove(key)
    }

    override fun instantiate(data: JsonObject): Emoji? {
        val guildId = data["guild_id"].optString
        return if (guildId != null) {
            GuildEmoji(client, data)
        } else {
            Emoji(client, data)
        }
    }
}