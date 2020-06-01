package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageUpdateEvent
import cn.troph.tomon.core.events.ReactionAddEvent
import cn.troph.tomon.core.structures.MessageReaction
import cn.troph.tomon.core.structures.TextChannel
import com.google.gson.JsonElement

class ReactionAddAction(client: Client) : Action<MessageReaction>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): MessageReaction? {
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["channel_id"].asString] as TextChannel
        val message = channel.messages[obj["message_id"].asString]
        if (message != null) {
            val emoji = obj["emoji"].asJsonObject
            val identifier: String =
                if (message!!.reactions.has(emoji["id"].asString)) emoji["id"].asString else "_${emoji["name"].asString}"
            val reaction =
                message!!.reactions[identifier]
            if (reaction != null) {
                reaction.update("count", reaction.count + 1)
                reaction.update("me", true)
                client.eventBus.postEvent(ReactionAddEvent(reaction = reaction))
                return reaction
            } else {
                val nReaction = message.reactions.add(obj)
                if (nReaction != null) {
                    nReaction.update("count", nReaction.count + 1)
                    if (obj["user_id"].asString == Client.global.me.id)
                        nReaction.update("me", true)
                    client.eventBus.postEvent(ReactionAddEvent(reaction = nReaction))
                }
                return nReaction
            }
        }
        return null

    }
}