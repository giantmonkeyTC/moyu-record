package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ReactionDeleteEvent
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.MessageReaction
import cn.troph.tomon.core.structures.TextChannel
import com.google.gson.JsonElement

class ReactionDeleteAction(client: Client) : Action<MessageReaction>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): MessageReaction? {
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["channel_id"].asString] as TextChannel
        val message = channel.messages[obj["message_id"].asString]
        val identifier: String =
            if (message!!.reactions.has(obj["id"].asString)) obj["id"].asString else "_${obj["name"]}"
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
            client.eventBus.postEvent(ReactionDeleteEvent(reaction = reaction))
        }
        return reaction
    }
}