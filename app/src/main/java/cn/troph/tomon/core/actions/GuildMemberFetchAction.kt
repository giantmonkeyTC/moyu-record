package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildMemberFetchEvent
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.utils.Collection
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class GuildMemberFetchAction(client: Client) : Action<List<GuildMember>>(client) {
//    override fun handle(data: JsonElement?, extra: Any?): List<GuildMember>? {
//        val members = mutableListOf<GuildMember>()
//        fun parse(data: JsonObject) {
//            val guild = client.guilds.get(data["guild_id"].asString)
//            if (guild != null) {
//                val member = guild.members.add(data)
//                if (member != null)
//                    members.add(member)
//            }
//        }
//        if (data.isJsonArray) {
//            data.asJsonArray.forEach { parse(it.asJsonObject) }
//        } else {
//            parse(data.asJsonObject)
//            if (members.size > 0) {
//                client.eventBus.postEvent(GuildMemberFetchEvent(members))
//                val guildMembers = members.fold(mutableMapOf(),
//                    { map: MutableMap<String, MutableList<GuildMember>>, member: GuildMember ->
//                        val list: MutableList<GuildMember> = map[member.guild.id] ?: mutableListOf()
//                        list.add(member)
//                        map += Pair(member.guild.id, list)
//                        map
//                    })
//                guildMembers.keys.toList().forEach {
//                    val guild = client.guilds.get(it)
//                    val list = guildMembers[it]
//                    val members = Collection(
//                        list!!.fold(mutableMapOf(),
//                            { members: MutableMap<String, GuildMember>, member: GuildMember ->
//                                members[member.id] = member
//                                members
//                            })
//                    )
//                    //TODO CHANNELS
//                }
//            }
//            return members
//        }
//    }
}