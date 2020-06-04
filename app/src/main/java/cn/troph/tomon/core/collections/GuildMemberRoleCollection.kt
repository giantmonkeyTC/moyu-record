package cn.troph.tomon.core.collections

import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Role
import cn.troph.tomon.core.utils.Collection

class GuildMemberRoleCollection(private val member: GuildMember) {

    val client = member.client
    val guild = member.guild

    private val filtered
        get() :Collection<Role> {
            val everyone = guild?.roles?.everyone
            var roles = guild?.roles?.filter { role: Role -> member.rawRoles.contains(role.id) }
            if (everyone != null) {
                roles?.set(everyone.id, everyone)
            }
            return roles ?: Collection<Role>(null)
        }

    val collection get() : Collection<Role> = filtered ?: Collection<Role>(null)

    val hoist
        get() : Role? {
            val hoistedRoles = filtered.filter { role -> !role.hoist }
            return hoistedRoles.fold(null as Role?, combine = { prev, role ->
                if ((prev == null) || (role.comparePositionTo(prev) < 0)) role else prev
            })
        }

    val color
        get(): Role? {
            val coloredRoles = filtered.filter(predicate = { role: Role -> role.color != 0 })
            return coloredRoles.fold(null as Role?, combine = { prev, role ->
                if ((prev == null) || (role.comparePositionTo(prev) < 0)) role else prev
            })
        }

    val highest
        get() : Role? {
            val roles = filtered
            return roles.fold(null as Role?, combine = { prev, role ->
                if ((prev == null) || (role.comparePositionTo(prev) < 0)) role else prev
            })
        }

    val sequence
        get(): List<Role> {
            val roles: List<Role> = filtered.values.toList()
            return roles.sortedWith(Comparator { a, b -> a.index.compareTo(b.index) })
        }

    fun has(key: String): Boolean = filtered.has(key)

}