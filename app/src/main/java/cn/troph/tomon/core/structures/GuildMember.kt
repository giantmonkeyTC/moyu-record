package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.GuildMemberRoleCollection
import cn.troph.tomon.core.utils.Converter
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
import java.time.LocalDateTime

class GuildMember(client: Client, data: JsonObject) : Base(client, data) {

    var id: String = ""
        private set
    var guildId: String = ""
        private set
    var nick: String? = null
        private set
    var joinedAt: LocalDateTime = LocalDateTime.now()
        private set
    var rawRoles: List<String> = listOf()
        private set

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("user")) {
            val user = client.users.add(data["user"].asJsonObject)
            id = user?.id ?: ""
        }
        if (data.has("guild_id")) {
            guildId = data["guild_id"].asString
        }
        if (data.has("nick")) {
            nick = data["nick"].optString;
        }
        if (data.has("joined_at")) {
            joinedAt = Converter.toDate(data["joined_at"].asString)
        }
        if (data.has("roles")) {
            rawRoles = if (data["roles"].isJsonNull) {
                listOf()
            } else {
                data["roles"].asJsonArray.map { ele -> ele.asString }
            }
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    val user get() = client.users[id]

    val guild get() = client.guilds[guildId]

    val roles get() : GuildMemberRoleCollection = GuildMemberRoleCollection(this)

    val displayName get() = (if (nick == null || nick == "") user?.name else nick) ?: ""

    val isOwner get() = id == guild?.ownerId ?: false

    fun hasRole(role: Role): Boolean {
        return hasRole(role.id)
    }

    fun hasRole(roleId: String): Boolean {
        return if (roleId == guildId) true else rawRoles.indexOf(roleId) != -1
    }
}