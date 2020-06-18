package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageAtMeEvent
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class MessageCreateAction(client: Client) : Action<Message>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): Message? {
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["channel_id"].asString] as TextChannel
        val existing = channel.messages.has(obj["id"].asString)
        val message = channel.messages.add(obj)
        if (!existing && message != null) {
            if (message.mentions.contains(client.me.id)) {
                client.eventBus.postEvent(MessageAtMeEvent(message = message))
            }
            channel.patch(JsonObject().apply {
                addProperty("last_message_id", message.id)
            })
            client.eventBus.postEvent(MessageCreateEvent(message = message))
        }
        return message
    }
}