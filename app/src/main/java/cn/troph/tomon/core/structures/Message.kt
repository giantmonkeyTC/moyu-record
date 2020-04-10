package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.MessageType
import cn.troph.tomon.core.collections.MessageReactionCollection
import cn.troph.tomon.core.utils.Collection
import java.time.LocalDateTime

class Message(client: Client, data: JsonData) : Base(client, data) {

    val id: String = data["id"] as String
    val channelId: String = data["channel_id"] as String
    val authorId: String? = null
    var type: MessageType = MessageType.DEFAULT
        private set
    var content: String? = null
        private set
    var timestamp: LocalDateTime = LocalDateTime.now()
        private set
    var nonce: String? = null
        private set
    var pending: Boolean = false
        private set
    val attachments: Collection<MessageAttachment> = Collection(null)
    val reactions: MessageReactionCollection = MessageReactionCollection(client, id)
    val mentions: Collection<User> = Collection(null)

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("type")) {
            val value = data["type"] as Int
            type = MessageType.fromInt(value) ?: MessageType.DEFAULT
        }
        if (data.contains("content")) {
            content = data["content"] as? String
        }
        if (data.contains("timestamp")) {
            val date = data["timestamp"] as String
            timestamp = LocalDateTime.parse(date)
        }
        if (data.contains("nonce")) {
            nonce = data["nonce"] as? String
        }
        if (data.contains("attachments")) {
            val array = data["attachments"] as JsonArray
            attachments.clear()
            array.forEach { at ->
                attachments.put(
                    at["id"] as String,
                    MessageAttachment(client, at)
                )
            }
        }
        if (data.contains("reactions")) {
            val array = data["reactions"] as JsonArray
            reactions.clear()
            array.forEach {
                reactions.add(it)
            }
        }
        if (data.contains("mentions")) {
            val array = data["mentions"] as JsonArray
            array.forEach { u ->
                val user = client.users.add(u)
                if (user != null) {
                    mentions.put(user.id, user)
                }
            }
        }
        if (data.contains("author") && data["author"] != null) {
            client.users.add(data)
        }
        if (data.contains("pending")) {
            pending = data["pending"] as Boolean
        }
    }

    val author: User?
        get() {
            val authorId = this.authorId
            return if (authorId != null) client.users.get(authorId) else null
        }

    val channel get() = client.channels.get(channelId)

    val guild get() : Guild? = if (this.channel is GuildChannel) (this.channel as GuildChannel).guild else null

}