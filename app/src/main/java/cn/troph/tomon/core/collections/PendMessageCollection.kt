package cn.troph.tomon.core.collections

import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.Message

class PendMessageCollection(val channel: Channel) : BaseCollection<Message>(channel.client) {

    override fun instantiate(data: JsonData): Message? {
        val channel = client.channels.get(data["channel_id"] as String) ?: error("no such channel")
        return Message(client, data, channel)
    }

    override fun add(
        data: JsonData,
        identify: ((d: JsonData) -> String)?
    ): Message? {
        val existing = get(data["id"] as String)
        if (existing != null) {
            existing.update(data)
            return existing
        }
        val entry = instantiate(data)
        if (entry != null && data.containsKey("nonce"))
            put(data["nonce"] as String, entry)
        return entry
    }
}