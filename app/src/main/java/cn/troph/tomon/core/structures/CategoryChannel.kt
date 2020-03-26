package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.utils.Collection

class CategoryChannel(client: Client, data: JsonData, guild: Guild) :
    GuildChannel(client, data, guild) {
//    val children get() : Collection<GuildChannel> = guild.channels.filter TODO

    override fun patch(data: JsonData) {
        super.patch(data)
    }

    override fun toString(): String {
        return "[CoreCategoryChannel $id] { name: $name }"
    }

}