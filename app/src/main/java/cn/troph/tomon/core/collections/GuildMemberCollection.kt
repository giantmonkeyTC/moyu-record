package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.User
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable

class GuildMemberCollection(client: Client, val guild: Guild) :
    BaseCollection<GuildMember>(client) {

    override fun add(
        data: JsonObject,
        identify: CollectionIdentify?
    ): GuildMember? {
        val id = data["user"].asJsonObject["id"].asString
        return super.add(data, identify ?: { _ -> id })
    }

    override fun instantiate(data: JsonObject): GuildMember? {
        return GuildMember(client, data)
    }


}