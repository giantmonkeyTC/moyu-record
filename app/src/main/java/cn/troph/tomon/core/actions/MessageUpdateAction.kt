package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageUpdateEvent
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import com.google.gson.JsonElement

class MessageUpdateAction(client: Client) : Action<Message>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): Message? {
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["channel_id"].asString] as TextChannel
        val message = channel.messages[obj["id"].asString]
        if (message != null) {
            message.update(obj)
            client.eventBus.postEvent(MessageUpdateEvent(message = message))
        }
        return message
    }
}