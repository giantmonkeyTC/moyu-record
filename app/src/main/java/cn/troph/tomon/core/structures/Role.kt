package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.Snowflake
import cn.troph.tomon.core.utils.optBoolean
import cn.troph.tomon.core.utils.optInt
import com.google.gson.JsonObject

class Role(client: Client, data: JsonObject) : Base(client, data) {

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

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("guild_id")) {
            guildId = data["guild_id"].asString
        }
        if (data.has("name")) {
            name = if (id == data["guild_id"].asString) "@所有人" else data["name"].asString
        }
        if (data.has("color")) {
            color = data["color"].optInt ?: 0
        }
        if (data.has("position")) {
            position = data["position"].asInt
        }
        if (data.has("hoist")) {
            hoist = data["hoist"].optBoolean ?: false
        }
        if (data.has("permissions")) {
            permissions = Permissions(data["permissions"].optInt ?: 0)
        }
        if (data.has("mentionable")) {
            mentionable = data["mentionable"].optBoolean ?: true
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
            return Snowflake.aligned(role1.id).compareTo(Snowflake.aligned(role2.id))
        }
    }

}