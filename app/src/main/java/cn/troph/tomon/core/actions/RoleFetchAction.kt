package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.RoleFetchEvent
import cn.troph.tomon.core.events.RoleSyncEvent
import cn.troph.tomon.core.structures.Role
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class RoleFetchAction(client: Client) : Action<List<Role>>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): List<Role>? {
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
        val guild = (if (guildId == "") null else client.guilds.get(guildId)) ?: return null
        // 如果同步，移除没有出现过的role
        if (isSync) {
            val keys = guild.roles.keys.asSequence().toMutableSet()
            val array = data!!.asJsonArray
            array.forEach {
                keys.remove(it.asJsonObject["id"].asString)
            }
            keys.forEach {
                guild.roles.remove(it)
            }
        }
        val roles = mutableListOf<Role>()
        val parse = { obj: JsonObject ->
            val role = guild.roles.add(obj)
            if (role != null) {
                roles.add(role)
            }
        }
        if (data!!.isJsonArray) {
            data!!.asJsonArray.forEach { parse(it.asJsonObject) }
        } else {
            parse(data!!.asJsonObject)
        }
        if (isSync) {
            client.eventBus.postEvent(RoleSyncEvent(guild))
        } else {
            client.eventBus.postEvent(RoleFetchEvent(roles))
        }
        return roles
    }
}