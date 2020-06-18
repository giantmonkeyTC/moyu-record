package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ReactionRemoveEvent
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.MessageReaction
import cn.troph.tomon.core.structures.TextChannel
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ReactionRemoveAction(client: Client) : Action<MessageReaction>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): MessageReaction? {
        var reaction: MessageReaction? = null
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["channel_id"].asString]
        if (channel is TextChannel) {
            reaction = textChannel(channel, obj)
        } else if (channel is DmChannel) {
            reaction = dmChannel(channel, obj)
        }

        return reaction
    }

    private fun textChannel(channel: TextChannel, obj: JsonObject): MessageReaction? {
        val message = channel.messages[obj["message_id"].asString]
        if (message != null) {
            val emoji = obj["emoji"].asJsonObject
            val identifier: String =
                if (message!!.reactions.has(emoji["id"].asString)) emoji["id"].asString else "_${emoji["name"].asString}"
            val reaction =
                message.reactions[identifier]
            if (reaction != null) {
                if (reaction.count - 1 != 0) {
                    reaction.update("count", reaction.count - 1)
                    reaction.update("me", false)
                } else {
                    reaction.update("count", reaction.count - 1)
                    message.reactions.remove(identifier)
                }
                client.eventBus.postEvent(ReactionRemoveEvent(reaction = reaction))
            }
            return reaction
        }
        return null
    }

    private fun dmChannel(channel: DmChannel, obj: JsonObject): MessageReaction? {
        val message = channel.messages[obj["message_id"].asString]
        if (message != null) {
            val emoji = obj["emoji"].asJsonObject
            val identifier: String =
                if (message!!.reactions.has(emoji["id"].asString)) emoji["id"].asString else "_${emoji["name"].asString}"
            val reaction =
                message.reactions[identifier]
            if (reaction != null) {
                if (reaction.count - 1 != 0) {
                    reaction.update("count", reaction.count - 1)
                    reaction.update("me", false)
                } else {
                    reaction.update("count", reaction.count - 1)
                    message.reactions.remove(identifier)
                }
                client.eventBus.postEvent(ReactionRemoveEvent(reaction = reaction))
            }
            return reaction
        }
        return null
    }
}