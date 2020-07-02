package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageAtMeEvent
import cn.troph.tomon.core.events.MessageUpdateEvent
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import com.google.gson.JsonElement

class MessageUpdateAction(client: Client) : Action<Message>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): Message? {
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["channel_id"].asString] as TextChannelBase
        val message = channel.messages[obj["id"].asString]
        if (message != null) {
            message.update(obj)
            if (message.mentions.contains(client.me.id) && channel is TextChannel) {
                channel.mention += 1
                client.eventBus.postEvent(MessageAtMeEvent(message = message))
            }
            client.eventBus.postEvent(MessageUpdateEvent(message = message))
        }
        return message
    }
}