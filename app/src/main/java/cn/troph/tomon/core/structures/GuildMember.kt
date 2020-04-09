package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.collections.GuildMemberRoleCollection
import java.time.Instant
import java.util.*

class GuildMember(client: Client, data: JsonData, val guild: Guild) : Base(client, data) {

    var id: String = ""
        private set
    var nick: String? = null
        private set
    var joinedAt: Date? = null
        private set
    var rawRoles: List<String> = listOf()
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("nick")) {
            nick = data["nick"] as String;
        }
        if (data.contains("joined_at")) {
            joinedAt = Date.from(Instant.parse(data["joined_at"] as String))
        }
        if (data.contains("user")) {
            val user = client.users.add(data["user"] as JsonData);
            id = user?.id ?: ""
        }
        if (data.contains("roles")) {
            rawRoles = data["roles"] as? List<String> ?: listOf();
        }
    }

    val user get() = client.users.get(id)

    val roles get() : GuildMemberRoleCollection = GuildMemberRoleCollection(this)

    val displayName get() = (if (nick == null || nick == "") user?.name else nick) ?: ""

    val isOwner get() = id == guild.ownerId

    fun hasRole(role: Role): Boolean {
        return if (role.isEveryone) true else rawRoles.indexOf(role.id) != -1
    }
}