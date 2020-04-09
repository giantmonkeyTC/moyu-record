package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class CategoryChannel(client: Client, data: JsonData) :
    GuildChannel(client, data) {

    val children get() = guild?.channels?.filter { c: Channel -> c is GuildChannel && c.parentId == id }

}