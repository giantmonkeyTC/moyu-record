package cn.troph.tomon.core.structures

import android.nfc.FormatException
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class Role(client: Client, data: JsonData) : Base(client, data) {
    var id: String = ""
    var guildId: String = ""
    var name: String = ""
    var color: Int = 0
    var position: Int = 0
    var hoist: Boolean = false
    var permissions: Permissions = Permissions(0)
    var mentionable: Boolean = true


    // TODO DELETE, UPDATE, ETC.
    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("id")) {
            id = data["id"] as String
        }
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

    val index
        get() : Int {
            return guild!!.roles.list().indexOf(this)
        }

    val guild get() = client.guilds.get(guildId)

    val isEveryone get() = id == guild?.id

    fun comparePositionTo(role: Any): Int {
        var roleInstance = this.guild?.roles?.resolve(role)
        if (roleInstance == null)
            return throw FormatException("Role nor a Snowflake")
        return comparePositions(this, roleInstance as Role)

    }

    fun comparePositions(role1: Role, role2: Role): Int {
        if (role1.isEveryone != role2.isEveryone)
            return (if (role1.isEveryone) 1 else 0) - (if (role1.isEveryone) 1 else 0)
        if (role1.position != role2.position)
            return role2.position - role1.position
        return role1.id.compareTo(role2.id)
    }

    override fun toString(): String {
        return "[CoreRole $id] { name: $name }"
    }


}