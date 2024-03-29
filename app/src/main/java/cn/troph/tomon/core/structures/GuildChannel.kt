package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.ChannelMemberCollection
import cn.troph.tomon.core.events.VoiceStateUpdateEvent
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.optString
import cn.troph.tomon.core.utils.snowflake
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.*
import kotlin.Comparator

open class GuildChannel(client: Client, data: JsonObject) : Channel(client, data),
    Comparable<GuildChannel>{

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
    var parentId: String? = null
    var permissionOverwrites: Collection<PermissionOverwrites> = Collection()
        private set

    open val members: ChannelMemberCollection = ChannelMemberCollection(this)

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
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

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    val guild get(): Guild? = client.guilds.get(guildId ?: "")

    val parent get() : CategoryChannel? = guild?.channels?.get(parentId ?: "") as? CategoryChannel

    fun overwritesForMember(member: GuildMember): MemberPermissionOverwrites {
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
        val everyoneOverwrites = permissionOverwrites[guild!!.id]
        val roleOverwrites = permissionOverwrites[role.id]
        return role.permissions
            .minus(everyoneOverwrites?.deny)
            .plus(everyoneOverwrites?.allow)
            .minus(roleOverwrites?.deny)
            .plus(roleOverwrites?.allow)
    }

    fun deletePermissionOverwritesLocal(id: String) {
        if (!permissionOverwrites.has(id))
            return
        val newOverwrites = permissionOverwrites.clone()
        newOverwrites.remove(id)
        val overwrites = newOverwrites.map { _, overwrites ->
            mutableMapOf<String, Any>().apply {
                this["id"] = overwrites.id
                this["type"] = overwrites.type
                this["allow"] = overwrites.allow
                this["deny"] = overwrites.deny
            }
        }
        client.actions.channelUpdate(JsonParser.parseString("""{"id": id, "permission_overwrites": overwrites}""").asJsonObject)
    }

    val isPrivate: Boolean get() {
        if (guildId != null) {
            val everyoneOverwrite = permissionOverwrites[guildId!!]
            return (everyoneOverwrite?.deny ?: 0L and Permissions.VIEW_CHANNEL) != 0L
        }
        return false
    }

    val indent
        get() : Int {
            var indent: Int = 0
            var cursor: GuildChannel? = parent
            while (cursor != null) {
                cursor = cursor.parent
                indent++
            }
            return indent
        }

    private fun comparePositionTo(other: GuildChannel): Int {
        val comparator = Comparator<GuildChannel> { o1, o2 ->
            o1.position.compareTo(o2.position)
        }.then(Comparator { o1, o2 ->
            o1.id.snowflake.compareTo(o2.id.snowflake)
        })
        return comparator.compare(this, other)
    }

    val path: List<GuildChannel> get() {
        val list = LinkedList<GuildChannel>()
        var cursor: GuildChannel? = this
        while (cursor != null) {
            list.push(cursor)
            cursor = cursor.parent
        }
        return list
    }

    override fun compareTo(other: GuildChannel): Int {
        val path = this.path
        val otherPath = other.path
        val length = path.size.coerceAtMost(otherPath.size)
        for (i in 0 until length) {
            val comp = path[i].comparePositionTo(otherPath[i])
            if (comp != 0) {
                return comp
            }
        }
        return path.size.compareTo(otherPath.size)
    }

}