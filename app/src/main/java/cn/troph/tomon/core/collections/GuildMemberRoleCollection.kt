package cn.troph.tomon.core.collections

import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Role
import cn.troph.tomon.core.utils.Collection

class GuildMemberRoleCollection(val member:GuildMember) {
    val client = member.client
    val guild = member.guild

    val filtered get() :Collection<Role>{
        val everyone = guild.roles.everyone
        var roles = TODO()
        if (everyone!=null)
            roles[everyone.id] = everyone
        return roles
    }

    val collection get() : Collection<Role> = filtered

    val hoist get():Role?{
        val hoistedRoles = filtered.filter { role -> !role.hoist  }
        if (hoistedRoles.length == 0)
            return null
//        return hoistedRoles.fold(null,combine = {prev,role->(prev==null) || if(role.)})

    }
}