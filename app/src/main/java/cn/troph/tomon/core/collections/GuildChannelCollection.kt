package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.utils.SortedList
import com.google.gson.JsonObject

class GuildChannelCollection(client: Client, val guild: Guild) :
    BaseCollection<GuildChannel>(client) {

    // forbid add
    override fun add(
        data: JsonObject,
        identify: CollectionIdentify?
    ): GuildChannel? {
        return null
    }

}