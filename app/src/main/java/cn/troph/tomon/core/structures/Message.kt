package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.utils.Collection
import java.time.Instant
import java.util.*

class Message(client: Client, data: JsonData) : Base(client, data) {
    var channel: Channel? = null
    var id: String = ""
    var author: User? = null
    var type: Int = 0
    var content: String = ""
    var timestamp: Date? = null
    var nonce: String = ""
    var pending: Boolean = false
    var attachments: cn.troph.tomon.core.utils.Collection<MessageAttachment>? = null
    var reactions: Collection<Reaction>? = null

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.containsKey("id")) {
            id = data["id"] as String
        }
        if (data.containsKey("type")) {
            type = data["type"] as Int
        }
        if (data.containsKey("content")) {
            content = data["content"] as String
        }
        if (data.containsKey("timestamp")) {
            timestamp = Date.from(Instant.parse(data["timestamp"] as String))
        }
        if (data.containsKey("nonce")) {
            nonce = data["nonce"] as String
        }
        if (data.containsKey("attachments")) {
            attachments?.clear()
            for (attachment in data["attachments"] as Map<String, JsonData>) {
                attachments?.put(
                    (attachment as JsonData)["id"] as String,
                    MessageAttachment(client, attachment)
                )
            }
        }
        if (data.containsKey("pending")) {
            pending = data["pending"] as Boolean
        }
        if (data.containsKey("reactions")) {
            reactions?.clear()
            for (reaction in data["reactions"] as JsonData) {
                reactions?.put(
                    (reaction as Map<String, JsonData>)["emoji"]?.get("name") as String,
                    Reaction(client, reaction)
                )
            }
        }
        if (data.containsKey("author") && data["author"] != null) {
            author = User(client, data["author"] as JsonData)
        }
    }

    val guild get() : Guild = TODO()

    override fun toString(): String {
        return "[CoreMessage $id] { channel: ${channel?.id} }"
    }
}