package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.GuildMemberRoleCollection
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

    override fun patch(data: JsonObject) {
        super.patch(data)
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
            joinedAt = LocalDateTime.parse(data["joined_at"].asString)
        }
        if (data.has("roles")) {
            rawRoles = if (data["roles"].isJsonNull) {
                listOf()
            } else {
                data["roles"].asJsonArray.map { ele -> ele.asString }
            }
        }
    }

    val user get() = client.users.get(id)

    val guild get() = client.guilds.get(guildId)

    val roles get() : GuildMemberRoleCollection = GuildMemberRoleCollection(this)

    val displayName get() = (if (nick == null || nick == "") user?.name else nick) ?: ""

    val isOwner get() = id == guild?.ownerId ?: false

    fun hasRole(role: Role): Boolean {
        return if (role.isEveryone) true else rawRoles.indexOf(role.id) != -1
    }
}