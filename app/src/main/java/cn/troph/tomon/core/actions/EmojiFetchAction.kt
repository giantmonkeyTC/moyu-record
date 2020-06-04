package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.EmojiFetchEvent
import cn.troph.tomon.core.events.EmojiSyncEvent
import cn.troph.tomon.core.structures.GuildEmoji
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger

class EmojiFetchAction(client: Client) : Action<List<GuildEmoji>>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): List<GuildEmoji>? {
        val isSync: Boolean = (extras[0] as? Boolean) ?: true
        val getGuildId = { data: JsonElement ->
            if (data.isJsonArray) {
                data.asJsonArray.get(0)?.asJsonObject?.get("guild_id")?.optString
            } else {
                data.asJsonObject["guild_id"].optString
            }
        }
        val guildId: String =
            extras[1] as? String ?: (getGuildId(data!!) ?: "")
        val guild = (if (guildId == "") null else client.guilds[guildId]) ?: return null
        // 如果同步，移除没有出现过的emoji
        if (isSync) {
            val keys = guild.roles.keys.asSequence().toMutableSet()
            val array = data!!.asJsonArray
            array.forEach {
                keys.remove(it.asJsonObject["id"].asString)
            }
            keys.forEach {
                guild.emojis.remove(it)
            }
        }
        val emojis = mutableListOf<GuildEmoji>()
        val parse = { obj: JsonObject ->
            val emoji = client.emojis.add(obj)
            if (emoji is GuildEmoji) {
                emojis.add(emoji)
            }
        }
        if (data!!.isJsonArray) {
            data.asJsonArray.forEach { parse(it.asJsonObject) }
        } else {
            parse(data.asJsonObject)
        }
        if (isSync) {
            client.eventBus.postEvent(EmojiSyncEvent(guild))
        } else {
            client.eventBus.postEvent(EmojiFetchEvent(emojis))
        }
        return emojis
    }
}