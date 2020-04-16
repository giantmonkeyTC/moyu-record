package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageType
import cn.troph.tomon.core.collections.MessageReactionCollection
import cn.troph.tomon.core.utils.*
import cn.troph.tomon.core.utils.Collection
import com.google.gson.JsonObject
import java.time.LocalDateTime

class Message(client: Client, data: JsonObject) : Base(client, data),
    Comparable<Message> {

    var id: String = ""
        private set
    var channelId: String = ""
        private set
    var authorId: String? = null
        private set
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
    var attachments: Collection<MessageAttachment> = Collection()
        private set
    var mentions: Collection<User> = Collection()
        private set

    lateinit var reactions: MessageReactionCollection
        private set

    override fun init(data: JsonObject) {
        super.init(data)
        reactions = MessageReactionCollection(client, this)
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("channel_id")) {
            channelId = data["channel_id"].asString
        }
        if (data.has("type")) {
            val value = data["type"].asInt
            type = MessageType.fromInt(value) ?: MessageType.DEFAULT
        }
        if (data.has("author") && !data["author"].isJsonNull) {
            val author = client.users.add(data)
            authorId = author?.id
        }
        if (data.has("content")) {
            content = data["content"].optString
        }
        if (data.has("timestamp")) {
            timestamp = Converter.toDate(data["timestamp"].asString)
        }
        if (data.has("nonce")) {
            nonce = data["nonce"].optString
        }
        if (data.has("attachments")) {
            attachments = Collection()
            val array = data["attachments"].asJsonArray
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
        return id.snowflake.compareTo(other.id.snowflake)
    }

}