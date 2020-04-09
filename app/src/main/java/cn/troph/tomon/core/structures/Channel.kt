package cn.troph.tomon.core.structures

import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

abstract class Channel(client: Client, data: JsonData) : Base(client, data) {
    val id: String = data["id"] as String
    var type: ChannelType = ChannelType.TEXT
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("type")) {
            type = ChannelType.fromInt(data["type"] as Int) ?: ChannelType.TEXT
        }
    }
}
