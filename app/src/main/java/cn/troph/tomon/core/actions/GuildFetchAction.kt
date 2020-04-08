package cn.troph.tomon.core.actions

import android.text.BoringLayout
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.collections.GuildCollection
import cn.troph.tomon.core.events.GuildFetchEvent
import cn.troph.tomon.core.events.GuildSyncEvent
import cn.troph.tomon.core.structures.Guild

class GuildFetchAction(client: Client) : Action<List<Guild>>(client) {
    override fun handle(data: Any, extra: Any?): List<Guild>? {
        var isSync: Boolean = (extra as Boolean?) ?: true
        if (isSync)
            client.guilds.clear()
        val guilds = mutableListOf<Guild>()
        if (data is List<*>) {
            for (obj in data) {
                val guild = client.guilds.add(obj as JsonData)
                if (guild != null)
                    guilds.add(guild)
            }
        } else if (data is Map<*, *>) {
            val guild = client.guilds.add(data as JsonData)
            if (guild != null)
                guilds.add(guild)
        }
        if (isSync)
            client.eventBus.postEvent(GuildSyncEvent())
        else
            client.eventBus.postEvent(GuildFetchEvent(guilds))
        return guilds
    }
}