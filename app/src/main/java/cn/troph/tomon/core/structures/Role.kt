package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class Role(client: Client, data: JsonData) : Base(client, data) {

    val id: String = data["id"] as String
    var guildId: String = ""
        private set
    var name: String = ""
        private set
    var color: Int = 0
        private set
    var position: Int = 0
        private set
    var hoist: Boolean = false
        private set
    var permissions: Permissions = Permissions(0)
        private set
    var mentionable: Boolean = true
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("guild_id")) {
            guildId = data["guild_id"] as String
        }
        if (data.contains("name")) {
            name = if (id == data["guild_id"]) "@所有人" else data["name"] as String
        }
        if (data.contains("color")) {
            color = data["color"] as Int? ?: 0
        }
        if (data.contains("position")) {
            position = data["position"] as Int
        }
        if (data.contains("hoist")) {
            hoist = data["hoist"] as Boolean? ?: false
        }
        if (data.contains("permissions")) {
            permissions = Permissions(data["permissions"] as Int? ?: 0)
        }
        if (data.contains("mentionable")) {
            mentionable = data["mentionable"] as Boolean? ?: true
        }
    }

    val guild get() = client.guilds.get(guildId)

    val isEveryone get() = id == guild?.id

    val index get() = guild!!.roles.list().indexOf(this)

    // TODO DELETE, UPDATE, ETC.

    fun comparePositionTo(role: Role): Int {
        return comparePositions(this, role)
    }

    companion object {
        fun comparePositions(role1: Role, role2: Role): Int {
            if (role1.isEveryone != role2.isEveryone)
                return (if (role1.isEveryone) 1 else 0) - (if (role1.isEveryone) 1 else 0)
            if (role1.position != role2.position)
                return role2.position - role1.position
            // TODO string aligned
            return role1.id.compareTo(role2.id)
        }
    }

}