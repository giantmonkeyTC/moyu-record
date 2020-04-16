package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.ChannelMemberCollection
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.optString
import com.google.gson.Gson
import com.google.gson.JsonObject

open class GuildChannel(client: Client, data: JsonObject) : Channel(client, data) {

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
    var permissionOverwrites: Collection<PermissionOverwrites> = Collection()
        private set

    open val members: ChannelMemberCollection = ChannelMemberCollection(this)

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("name")) {
            name = data["name"].asString
        }
        if (data.has("guild_id")) {
            guildId = data["guild_id"].optString
        }
        if (data.has("position")) {
            position = data["position"].asInt
        }
        if (data.has("parent_id")) {
            parentId = data["parent_id"].optString
            if (parentId == "0") {
                parentId = null
            }
        }
        if (data.has("permission_overwrites")) {
            permissionOverwrites = Collection()
            val list = data["permission_overwrites"].asJsonArray
            list.forEach { obj ->
                var overwrite = obj.asJsonObject
                permissionOverwrites.set(
                    overwrite["id"].asString,
                    PermissionOverwrites(client, overwrite)
                )
            }
            val gid = data["guild_id"].optString
            if (gid != null && !permissionOverwrites.has(gid)) {
                val everyone = Gson().toJsonTree(
                    mapOf(
                        "id" to gid,
                        "type" to "role",
                        "allow" to 0,
                        "deny" to 0
                    )
                ).asJsonObject
                permissionOverwrites.set(
                    gid,
                    PermissionOverwrites(
                        client,
                        everyone
                    )
                )

            }
        }
    }

    val guild get(): Guild? = client.guilds.get(guildId ?: "")

    val parent get() : CategoryChannel? = guild?.channels?.get(parentId ?: "") as? CategoryChannel

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