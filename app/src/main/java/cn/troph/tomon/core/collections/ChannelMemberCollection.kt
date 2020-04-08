package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Role
import kotlinx.coroutines.flow.combine

class ChannelMemberCollection(val channel: GuildChannel) :
    BaseCollection<GuildMember>(channel.client) {
    val lastPullIndex = -1
    fun addMember(member: GuildMember) {
        this.set(member.id, member)
    }

    fun list(): List<String> {
        val members = values.toMutableList()
        val list = channel.guild!!.roles.list().filter { it.hoist }
        val indices =
            list.fold(mutableMapOf(), { map: MutableMap<String, Int>, role: Role ->
                val index = list.indexOf(role)
                map += Pair(role.id, index)
                map
            })
        members.sortWith(Comparator { aMember, bMember ->
            val aHoist = aMember.roles.hoist
            val bHoist = bMember.roles.hoist
            var compare: Int =
                (if (aHoist != null) indices[aHoist.id] else 10000)!!.compareTo(if (bHoist != null) indices[bHoist.id]!! else 10000)
            if (compare == 0)
                compare = aMember.displayName!!.compareTo(bMember.displayName!!)
            if (compare == 0)
                compare = aMember.id.compareTo(bMember.id)
            compare
        })

        val idList: MutableList<String> = mutableListOf()
        members.forEach { idList.add(it.id) }
        return idList
    }
}