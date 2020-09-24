package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageDeleteEvent
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class MessageDeleteAction(client: Client) : Action<Message>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): Message? {
        var msg: Message? = null
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["channel_id"].asString]
        if (channel is TextChannel) {
            textChannel(channel, obj)
        } else if (channel is DmChannel) {
            dmChannel(channel, obj)
        }
        return msg
    }

    private fun dmChannel(channel: DmChannel, obj: JsonObject): Message? {
        val message = channel.messages[obj["id"].asString]
        if (message != null) {
            if (message.id == channel.lastMessageId) {
                val index = channel.messages.getSortedList().indexOfFirst { it.id == channel.lastMessageId }
                channel.patch(JsonObject().apply {
                    addProperty("last_message_id", channel.messages.getSortedList()[index - 1].id)
                })
            }
            channel.messages.remove(obj["id"].asString)
            client.eventBus.postEvent(MessageDeleteEvent(message = message))
        }
        return message
    }

    private fun textChannel(channel: TextChannel, obj: JsonObject): Message? {
        val message = channel.messages[obj["id"].asString]
        if (message != null) {
            if (message.mentions.contains(client.me.id)) {
                channel.mention -= 1
            }
            if (message.id == channel.lastMessageId) {
                val index = channel.messages.getSortedList().indexOfFirst { it.id == channel.lastMessageId }
                channel.patch(JsonObject().apply {
                    addProperty("last_message_id", channel.messages.getSortedList()[index - 1].id)
                })
            }
            channel.messages.remove(obj["id"].asString)
            client.eventBus.postEvent(MessageDeleteEvent(message = message))
        }
        return message
    }
}