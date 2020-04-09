package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.events.GuildFetchEvent
import cn.troph.tomon.core.events.GuildSyncEvent
import cn.troph.tomon.core.structures.Guild

class GuildFetchAction(client: Client) : Action<List<Guild>>(client) {
    override fun handle(data: Any?, extra: Any?): List<Guild>? {
        var isSync: Boolean = (extra as Boolean?) ?: true
        // 如果同步，移除没有出现过的guild
        if (isSync) {
            val keys = client.guilds.keys.asSequence().toMutableSet()
            val array = data as JsonArray
            array.forEach {
                keys.remove(it["id"] as String)
            }
            keys.forEach {
                client.guilds.remove(it)
            }
        }
        val guilds = mutableListOf<Guild>()
        val parse = { data: JsonData ->
            val guild = client.guilds.add(data)
            if (guild != null) {
                guilds.add(guild)
            }
        }
        // 如果是数组，解析多个，否则解析一个
        if (data is List<*>) {
            (data as JsonArray).forEach { parse(it) }
        } else {
            parse(data as JsonData)
        }
        // 同步和拉取分开处理
        if (isSync) {
            client.eventBus.postEvent(GuildSyncEvent())
        } else {
            client.eventBus.postEvent(GuildFetchEvent(guilds))
        }
        return guilds
    }
}