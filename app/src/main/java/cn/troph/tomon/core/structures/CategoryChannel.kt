package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

class CategoryChannel(client: Client, data: JsonObject) :
    GuildChannel(client, data) {

    val children get() = guild?.channels?.filter { c: Channel -> c is GuildChannel && c.parentId == id }

}