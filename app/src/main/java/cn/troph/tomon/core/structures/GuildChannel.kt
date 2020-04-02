package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.collections.ChannelMemberCollection
import cn.troph.tomon.core.collections.GuildMemberRoleCollection
import cn.troph.tomon.core.utils.BitField
import cn.troph.tomon.core.utils.Collection

open class GuildChannel(client: Client, data: JsonData, val guild: Guild?) : Channel(client, data) {
    var name: String = ""
    var position: Int = 0
    var parentId: String = ""
    var permissionOverwrites: Collection<PermissionOverwrites>? = null
    var members = ChannelMemberCollection(this)

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
        if (data.containsKey("permission_overwrites")) {
            permissionOverwrites?.clear()
            val list = data["permission_overwrites"] as List<*>
            for (obj in list) {
                var overwrite = obj as MutableMap<String, String>
                permissionOverwrites?.set(
                    overwrite["id"]!!,
                    PermissionOverwrites(this.client, overwrite, this)
                )
            }
            if (data.containsKey("guild_id") &&
                !permissionOverwrites!!.has(data["guild_id"] as String)
            ) {

                permissionOverwrites!!.set(
                    data["guild_id"] as String,
                    PermissionOverwrites(
                        this.client,
                        mapOf<String, Any>(
                            "id" to (data["guild_id"] ?: error("guild_id is not a key")),
                            "type" to "role",
                            "allow" to 0,
                            "deny" to 0
                        ), this
                    )
                )

            }
        }
    }

    val parent get() : GuildChannel? = guild?.channels?.get(parentId)

    fun permissionFor(memberOrRole: GuildMember): Permissions? {
        val member = guild!!.members.resolve(memberOrRole)
        if (member != null)
            return memberPermissions(member as GuildMember)
        val role = guild.roles.resolve(memberOrRole)
        if (role != null)
            return rolePermissions(member as Role)
        return null
    }

    fun overwritesFor(
        member: GuildMember,
        roles: GuildMemberRoleCollection? = member.roles
    ): Map<String, Any?> {
        if (member == null)
            return mutableMapOf()
        var varRole = roles ?: member.roles
        var roleOverwrites = mutableListOf<PermissionOverwrites>()
        var memberOverwrites: PermissionOverwrites? = null
        var everyoneOverwrites: PermissionOverwrites? = null
        for (overwrite in permissionOverwrites!!.values) {
            if (overwrite.id == guild?.id)
                everyoneOverwrites = overwrite
            else if (roles!!.has(overwrite.id))
                roleOverwrites.add(overwrite)
            else if (overwrite.id == member.id)
                memberOverwrites = overwrite
        }
        return mapOf(
            "everyone" to everyoneOverwrites,
            "roles" to roleOverwrites,
            "member" to memberOverwrites
        )


    }

    fun memberPermissions(member: GuildMember): Permissions {
        if (member.isOwner)
            return Permissions(Permissions.all)
        val roles = member.roles
        val permissions =
            Permissions(roles.collection.map<Permissions> { _, role -> role.permissions })
        if (permissions.has(Permissions.administrator))
            return Permissions(Permissions.all)

        val overwrites = overwritesFor(member, roles)
        var overwritesEveryone: PermissionOverwrites =
            overwrites["everyone"] as PermissionOverwrites
        var overwritesRoles: List<PermissionOverwrites> =
            overwrites["roles"] as List<PermissionOverwrites>
        val overwritesMember: PermissionOverwrites =
            overwrites["member"] as PermissionOverwrites
        permissions.minus(BitField(if (overwritesEveryone != null) overwritesEveryone.deny else 0))
            .plus(BitField(if (overwritesEveryone != null) overwritesEveryone.allow else 0))
            .minus(BitField(if (overwritesRoles.size > 0) overwritesRoles.map { it.deny } else 0))
            .plus(BitField(if (overwritesRoles.size > 0) overwritesRoles.map { it.allow } else 0))
            .minus(BitField(if (overwritesMember != null) overwritesMember.deny else 0))
            .plus(BitField(if (overwritesMember != null) overwritesMember.allow else 0))
        return permissions

    }

    fun rolePermissions(role: Role): Permissions {
        if (role.permissions.has(Permissions.administrator)) {
            return Permissions(Permissions.all)
        }
        val everyoneOverwrites: PermissionOverwrites? =
            permissionOverwrites?.get(guild!!.id)
        val roleOverwrites: PermissionOverwrites? = permissionOverwrites?.get(role.id)
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

    val indent
        get() : Int {
            var indent: Int = 0
            var cursor: GuildChannel? = parent
            while (cursor != null) {
                cursor = cursor.parent!!
                indent++
            }
            return indent
        }

    override fun toString(): String {
        return "[CoreGuildChannel $id] { name: $name }"
    }

}