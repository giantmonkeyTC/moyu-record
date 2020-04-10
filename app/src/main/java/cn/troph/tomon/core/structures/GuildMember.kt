package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.collections.GuildMemberRoleCollection
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

class GuildMember(client: Client, data: JsonData) : Base(client, data) {

    val id: String = (data["user"] as JsonData)["id"] as String
    val guildId: String = data["guild_id"] as String
    var nick: String? = null
        private set
    var joinedAt: LocalDateTime = LocalDateTime.now()
        private set
    var rawRoles: List<String> = listOf()
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("nick")) {
            nick = data["nick"] as String;
        }
        if (data.contains("joined_at")) {
            joinedAt = LocalDateTime.parse(data["joined_at"] as String)
        }
        if (data.contains("user")) {
            client.users.add(data["user"] as JsonData);
        }
        if (data.contains("roles")) {
            rawRoles = data["roles"] as? List<String> ?: listOf();
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