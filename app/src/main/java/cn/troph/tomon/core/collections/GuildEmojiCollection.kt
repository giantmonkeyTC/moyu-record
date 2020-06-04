package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildEmoji
import com.google.gson.JsonObject

class GuildEmojiCollection(client: Client, val guild: Guild) :
    BaseCollection<GuildEmoji>(client) {

    // forbid add
    override fun add(
        data: JsonObject,
        identify: CollectionIdentify?
    ): GuildEmoji? {

        return null
    }
}