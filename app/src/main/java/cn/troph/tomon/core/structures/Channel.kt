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

    companion object {
        fun typeOf(value: Int): ChannelType? {
            val map = mapOf(
                0 to ChannelType.TEXT,
                1 to ChannelType.VOICE,
                2 to ChannelType.DM,
                3 to ChannelType.GROUP,
                4 to ChannelType.CATEGORY
            )
            return if (map.containsKey(value))
                map[value]
            else
                ChannelType.TEXT
        }
    }

    override fun toString(): String {
        return "[CoreChannel $id] { type: $type }"
    }
}
