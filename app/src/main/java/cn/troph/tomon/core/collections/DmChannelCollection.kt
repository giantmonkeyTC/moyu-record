package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Channel
import com.google.gson.JsonObject

class DmChannelCollection(client: Client) :
    BaseCollection<Channel>(client) {

    override fun add(
        data: JsonObject,
        identify: ((d: JsonObject) -> String)?
    ): Channel? {
        return null
    }
}