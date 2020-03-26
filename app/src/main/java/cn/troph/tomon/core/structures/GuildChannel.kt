package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.utils.BitField
import cn.troph.tomon.core.utils.Collection

open class GuildChannel(client: Client, data: JsonData, val guild: Guild?) : Channel(client, data) {
    //channelMemberCollection TODO
    var name: String = ""
    var position: Int = 0
    var parentId: String = ""
    var permissionOverwrites: Collection<PermissionOverwrites> = Collection<PermissionOverwrites>()

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.containsKey("name")) {
            name = data["name"] as String
        }
        if (data.containsKey("position")) {
            position = data["position"] as Int
        }
        if (data.containsKey("parent_id")) {
            parentId = data["parent_id"] as String
            if (parentId == "0") {
                parentId = null.toString()
            }
        }
//        if (data.containsKey("permission_overwrites")) {
//            permissionOverwrites.clear()
//            val list = data["permission_overwrites"] as List<*>
//            for ( obj in list) {
//                var overwrite = obj as Map<String, String>
//                permissionOverwrites.get(overwrite["id"]) =
//                    PermissionOverwrites(client, overwrite)
//            }
//            if (data.containsKey("guild_id") &&
//                !permissionOverwrites.has(data["guild_id"])) {
//                permissionOverwrites[data["guild_id"]] = PermissionOverwrites.fromJson(
//                    this, {
//                        "id": data["guild_id"] as String,
//                        "type": "role",
//                        "allow": 0,
//                        "deny": 0
//                    })
//            }
//        }
    }

    val parent get() : GuildChannel? = guild?.channels?.get(parentId)

    fun permissionFor(memberOrRole: GuildMember): Permissions {
        TODO()
    }

//    fun overwritesFor(member : GuildMember,roles : GuildMember? = member.roles) {
//      TODO
//    }

    fun memberPermissions(member: GuildMember): Permissions {
        TODO()
    }

    fun rolePermissions(role: Role): Permissions {
        if (role.permissions.has(Permissions.administrator)) {
            return Permissions(Permissions.all)
        }
        val everyoneOverwrites: PermissionOverwrites? = guild?.id?.let { permissionOverwrites.get(it) }//TODO let
        val roleOverwrites: PermissionOverwrites? = permissionOverwrites.get(role.id)
        role.permissions.minus(BitField(if (everyoneOverwrites != null) everyoneOverwrites.deny else 0))
            .plus(
                BitField(if (everyoneOverwrites != null) everyoneOverwrites.allow else 0)
            ).minus(
                BitField(if (roleOverwrites != null) roleOverwrites.deny else 0)
            ).plus(
                BitField(if (roleOverwrites != null) roleOverwrites.deny else 0)
            )
        return role.permissions
    }

    val indent get() : Int {
        var indent : Int = 0
        var cursor : GuildChannel? = parent
        while (cursor!=null){
            cursor = cursor.parent!!
            indent++
        }
        return indent
    }

    override fun toString(): String {
        return "[CoreGuildChannel $id] { name: $name }"
    }

}