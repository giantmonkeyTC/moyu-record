package cn.troph.tomon.core.structures

import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

abstract class Channel(client: Client, data: JsonData) : Base(client, data) {
    var id: String = ""
    var type: ChannelType = ChannelType.TEXT

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("id")) {
            id = data["id"] as String
        }
        if (data.contains("type")) {
            type = ChannelType.fromInt(data["type"] as Int) ?: ChannelType.TEXT
        }
    }
}