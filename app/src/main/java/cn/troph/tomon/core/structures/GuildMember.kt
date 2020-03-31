package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.collections.GuildMemberRoleCollection
import java.time.Instant
import java.util.*

class GuildMember(client: Client, data: JsonData, val guild: Guild) : Base(client, data) {
    var nick: String? = null
    var joinedAt: Date? = null
    var user: User? = null
    var rawRoles: List<*>? = null

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.containsKey("nick")) {
            nick = data["nick"] as String;
        }
        if (data.containsKey("joined_at")) {
            joinedAt = Date.from(Instant.parse(data["joined_at"] as String))
        }
        if (data.containsKey("user")) {
            user = client.users.add(data["user"] as Map<String, Any>);
        }
        if (data.containsKey("roles")) {
            rawRoles = data["roles"] as List<*>?;
        }
    }

    val id get() :String = user!!.id

    val roles get() : GuildMemberRoleCollection = GuildMemberRoleCollection(this)

    val displayName get() = if (nick == null || nick == "") user!!.name else nick

    val isOwner get() = id == guild.ownerId

    fun hasRole(role : Role) : Boolean{
        val roleId = guild.roles.resolveId(role){it.id}
        if(roleId == guild.id)
            return true
        return rawRoles!!.indexOf(roleId) != -1
    }

    override fun toString(): String {
        return "[CoreGuildMember ${user?.id}] { guild: ${guild.id} }"
    }
}