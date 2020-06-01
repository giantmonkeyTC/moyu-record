package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageDeleteEvent
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import com.google.gson.JsonElement

class MessageDeleteAction(client: Client) : Action<Message>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): Message? {
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["channel_id"].asString] as TextChannel
        val message = channel.messages[obj["id"].asString]
        if (message != null) {
            channel.messages.remove(obj["id"].asString)
            client.eventBus.postEvent(MessageDeleteEvent(message = message))
        }
        return message
    }
}