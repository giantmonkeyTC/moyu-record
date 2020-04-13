package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageType
import cn.troph.tomon.core.collections.MessageReactionCollection
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.Snowflake
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
import java.time.LocalDateTime

class Message(client: Client, data: JsonObject) : Base(client, data), Comparable<Message> {

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

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("type")) {
            val value = data["type"].asInt
            type = MessageType.fromInt(value) ?: MessageType.DEFAULT
        }
        if (data.has("content")) {
            content = data["content"].optString
        }
        if (data.has("timestamp")) {
            val date = data["timestamp"].asString
            timestamp = LocalDateTime.parse(date)
        }
        if (data.has("nonce")) {
            nonce = data["nonce"].optString
        }
        if (data.has("attachments")) {
            val array = data["attachments"].asJsonArray
            attachments.clear()
            array.forEach { ele ->
                val at = ele.asJsonObject
                attachments.put(
                    at["id"].asString,
                    MessageAttachment(client, at)
                )
            }
        }
        if (data.has("reactions")) {
            val array = data["reactions"].asJsonArray
            reactions.clear()
            array.forEach { ele ->
                val obj = ele.asJsonObject
                reactions.add(obj)
            }
        }
        if (data.has("mentions")) {
            val array = data["mentions"].asJsonArray
            array.forEach { ele ->
                val u = ele.asJsonObject
                val user = client.users.add(u)
                if (user != null) {
                    mentions.put(user.id, user)
                }
            }
        }
        if (data.has("author") && !data["author"].isJsonNull) {
            client.users.add(data)
        }
        if (data.has("pending")) {
            pending = data["pending"].asBoolean
        }
    }

    val author: User?
        get() {
            val authorId = this.authorId
            return if (authorId != null) client.users.get(authorId) else null
        }

    val channel get() = client.channels.get(channelId)

    val guild get() : Guild? = if (this.channel is GuildChannel) (this.channel as GuildChannel).guild else null

    override fun compareTo(other: Message): Int {
        val timeCompare = timestamp.compareTo(other.timestamp)
        return if (timeCompare == 0) {
            Snowflake.aligned(id).compareTo(Snowflake.aligned(other.id))
        } else {
            timeCompare
        }
    }

}