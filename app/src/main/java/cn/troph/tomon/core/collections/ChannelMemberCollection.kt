package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.structures.GuildMember

class ChannelMemberCollection(val channel: GuildChannel):BaseCollection<GuildMember>(channel.client) {
    val lastPullIndex = -1
    fun addMember(member : GuildMember){
//        this[member]
    }
}