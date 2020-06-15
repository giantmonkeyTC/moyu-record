package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.DmChannel
import com.google.gson.JsonObject

class DmChannelCollection(client: Client) :
    BaseCollection<DmChannel>(client) {

    override fun add(
        data: JsonObject,
        identify: CollectionIdentify?
    ): DmChannel? {
        return null
    }
}