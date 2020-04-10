package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.MessageReaction

class MessageReactionCollection(client: Client, private val messageId: String) :
    BaseCollection<MessageReaction>(client) {

    override fun add(data: JsonData, identify: ((d: JsonData) -> String)?): MessageReaction? {
        val emoji = data["emoji"] as? JsonData
        return if (emoji == null) {
            null
        } else {
            val key = MessageReaction.getKey(MessageReaction.parseEmojiData(emoji))
            super.add(data, identify ?: { _ -> key })
        }
    }

    override fun instantiate(data: JsonData): MessageReaction? {
        return MessageReaction(client, data, messageId)
    }
}