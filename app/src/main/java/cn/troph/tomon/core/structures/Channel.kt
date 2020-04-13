package cn.troph.tomon.core.structures

import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

abstract class Channel(client: Client, data: JsonObject) : Base(client, data) {
    val id: String = data["id"].asString
    var type: ChannelType = ChannelType.TEXT
        private set

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("type")) {
            type = ChannelType.fromInt(data["type"].asInt) ?: ChannelType.TEXT
        }
    }
}
