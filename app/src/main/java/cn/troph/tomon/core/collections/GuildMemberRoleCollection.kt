package cn.troph.tomon.core.collections

import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Role
import cn.troph.tomon.core.utils.Collection

class GuildMemberRoleCollection(val member: GuildMember) {
    val client = member.client
    val guild = member.guild

    val filtered
        get() :Collection<Role> {
            val everyone = guild.roles.everyone
            var roles = guild.roles.filter { role: Role -> !member.rawRoles!!.contains(role.id) }
            if (everyone != null)
                roles.set(everyone.id,everyone)
            return roles
        }

    val collection get() : Collection<Role> = filtered

    val hoist
        get():Role? {
            val hoistedRoles = filtered.filter { role -> !role.hoist }
            if (hoistedRoles.length == 0)
                return null
            return hoistedRoles.fold(null as Role?, combine = { prev: Role?, role: Role ->
                if ((prev == null) || (role.comparePositionTo(prev) < 0)) role else prev

            })
        }

    val color
        get(): Role? {
            val coloredRoles = filtered.filter(predicate = { role: Role -> role.color == 0 })
            if (coloredRoles.length == 0)
                return null
            return coloredRoles.fold(null as Role?, combine = { prev: Role?, role: Role ->
                if ((prev == null) || (role.comparePositionTo(prev) < 0)) role else prev
            })
        }

    val highest
        get() : Role? {
            val roles = filtered
            if (roles.length == 0)
                return null
            return roles.fold(null as Role?, combine = { prev: Role?, role: Role ->
                if ((prev == null) || (role.comparePositionTo(prev) < 0)) role else prev
            })
        }

    val getSequence
        get(): Iterable<Role> {
            val roles: List<Role> = filtered.values.toList()
            return roles.sortedWith(Comparator { a: Role, b: Role -> a.index.compareTo(b.index) })
        }

    fun has(key: String): Boolean = filtered.has(key)

}