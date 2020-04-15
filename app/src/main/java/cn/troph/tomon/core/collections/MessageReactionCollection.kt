package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.MessageReaction
import com.google.gson.JsonObject

class MessageReactionCollection(client: Client, val message: Message) :
    BaseCollection<MessageReaction>(client) {

    override fun add(data: JsonObject, identify: ((d: JsonObject) -> String)?): MessageReaction? {
        val emoji = data["emoji"] as? JsonObject
        return if (emoji == null) {
            null
        } else {
            val key = MessageReaction.getKey(MessageReaction.parseEmojiData(emoji))
            super.add(data, identify ?: { _ -> key })
        }
    }

    override fun instantiate(data: JsonObject): MessageReaction? {
        return MessageReaction(client, data, message.id)
    }
}