package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.collections.ChannelMemberCollection
import cn.troph.tomon.core.utils.Collection

open class GuildChannel(client: Client, data: JsonData) : Channel(client, data) {

    data class MemberPermissionOverwrites(
        val everyone: PermissionOverwrites? = null,
        val member: PermissionOverwrites? = null,
        val roles: List<PermissionOverwrites> = listOf()
    )

    var name: String = ""
        private set
    var guildId: String? = null
        private set
    var position: Int = 0
        private set
    var parentId: String? = null
        private set
    val permissionOverwrites: Collection<PermissionOverwrites> =
        Collection<PermissionOverwrites>(null)
    val members = ChannelMemberCollection(this)

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("name")) {
            name = data["name"] as String
        }
        if (data.contains("guild_id")) {
            guildId = data["guild_id"] as? String
        }
        if (data.contains("position")) {
            position = data["position"] as Int
        }
        if (data.contains("parent_id")) {
            parentId = data["parent_id"] as? String
            if (parentId == "0") {
                parentId = null
            }
        }
        if (data.contains("permission_overwrites")) {
            permissionOverwrites.clear()
            val list = data["permission_overwrites"] as List<*>
            for (obj in list) {
                var overwrite = obj as JsonData
                permissionOverwrites.set(
                    overwrite["id"] as String,
                    PermissionOverwrites(client, overwrite)
                )
            }
            val gid = data["guild_id"] as? String
            if (gid != null && !permissionOverwrites.has(gid)) {
                permissionOverwrites.set(
                    gid,
                    PermissionOverwrites(
                        client,
                        mapOf(
                            "id" to gid,
                            "type" to "role",
                            "allow" to 0,
                            "deny" to 0
                        )
                    )
                )

            }
        }
    }

    val guild get(): Guild? = client.guilds.get(guildId ?: "")

    val parent get() : GuildChannel? = guild?.channels?.get(parentId ?: "")

    fun overwritesForMember(member: GuildMember): MemberPermissionOverwrites {
        if (member == null) {
            return MemberPermissionOverwrites()
        }
        var roleOverwrites = mutableListOf<PermissionOverwrites>()
        var memberOverwrites: PermissionOverwrites? = null
        var everyoneOverwrites: PermissionOverwrites? = null
        permissionOverwrites.forEach { overwrite ->
            when {
                overwrite.id == guildId -> everyoneOverwrites = overwrite
                member.roles.has(overwrite.id) -> roleOverwrites.add(overwrite)
                overwrite.id == member.id -> memberOverwrites = overwrite
            }
        }
        return MemberPermissionOverwrites(
            everyone = everyoneOverwrites,
            roles = roleOverwrites,
            member = memberOverwrites
        )
    }

    fun permissionForMember(member: GuildMember): Permissions? {
        // 非本群
        if (guild != member.guild) {
            return null
        }
        // 所有者
        if (member.isOwner) {
            return Permissions.all()
        }
        val permissions =
            Permissions(
                member.roles.collection.map { _, role -> role.permissions.value }
            )
        if (permissions.has(Permissions.ADMINISTRATOR)) {
            return Permissions.all()
        }
        val overwrites = overwritesForMember(member)
        return permissions
            .minus(overwrites.everyone?.deny)
            .plus(overwrites.everyone?.allow)
            .minus(overwrites.roles.map { role -> role.deny })
            .plus(overwrites.roles.map { role -> role.allow })
            .minus(overwrites.member?.deny)
            .plus(overwrites.member?.allow)
    }

    fun permissionForRole(role: Role): Permissions {
        if (role.permissions.has(Permissions.ADMINISTRATOR)) {
            return Permissions.all()
        }
        val everyoneOverwrites = permissionOverwrites.get(guild!!.id)
        val roleOverwrites = permissionOverwrites.get(role.id)
        return role.permissions
            .minus(everyoneOverwrites?.deny)
            .plus(everyoneOverwrites?.allow)
            .minus(roleOverwrites?.deny)
            .plus(roleOverwrites?.allow)
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

}