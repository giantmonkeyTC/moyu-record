package cn.troph.tomon.core.collections

import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.Message

class PendMessageCollection(val channel: Channel) : BaseCollection<Message>(channel.client) {

    override fun instantiate(data: Map<String, Any>): Message? {
        val channel = client.channels.get(data["channel_id"] as String)?: error("no such channel")
        return Message(client,data,channel)
    }

    override fun add(
        data: Map<String, Any>,
        identify: ((d: Map<String, Any>) -> String)?
    ): Message? {
        val existing = get(data["id"] as String)
        if (existing!=null){
            existing.patch(data)
            return existing
        }
        val entry = instantiate(data)
        if (entry!=null && data.containsKey("nonce"))
            put(data["nonce"] as String,entry)
        return entry
    }
}