package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.utils.SortedList
import com.google.gson.JsonObject

class GuildChannelCollection(client: Client, val guild: Guild) :
    BaseCollection<GuildChannel>(client) {

    private val sortedList: SortedList<GuildChannel> =
        SortedList(Comparator { o1, o2 -> o1.compareTo(o2) })

    // forbid add
    override fun add(
        data: JsonObject,
        identify: ((d: JsonObject) -> String)?
    ): GuildChannel? {
        return null
    }

    override fun set(key: String, value: GuildChannel): GuildChannel? {
        val ins = super.set(key, value)
        if (value != null) {
            sortedList.add(value)
        }
        return ins
    }

    override fun remove(key: String): GuildChannel? {
        val ins = super.remove(key)
        if (ins != null) {
            sortedList.remove(ins)
        }
        return ins
    }

    val list: SortedList.Immutable<GuildChannel> = sortedList.immutable

}