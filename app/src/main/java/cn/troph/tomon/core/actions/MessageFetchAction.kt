package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageFetchEvent
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class MessageFetchAction(client: Client) : Action<List<Message>>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): List<Message>? {
        val gotBeginning: Boolean = (extras[0] as? Boolean) ?: true
        val getChannelId = { data: JsonElement ->
            if (data.isJsonArray) {
                data.asJsonArray.get(0)?.asJsonObject?.get("channel_id")?.optString
            } else {
                data.asJsonObject["channel_id"].optString
            }
        }
        val channelId = extras[1] as? String ?: (getChannelId(data!!) ?: "")
        val channel = (if (channelId == "") null else client.channels[channelId]) ?: return null
        if (channel !is TextChannelBase) {
            return null
        }
        val messages = mutableListOf<Message>()
        val parse = { obj: JsonObject ->
            val message = channel.messages.add(obj)
            if (message != null) {
                messages.add(message)
            }
        }
        if (data!!.isJsonArray) {
            data!!.asJsonArray.forEach { parse(it.asJsonObject) }
        } else {
            parse(data!!.asJsonObject)
        }
        if (messages.isNotEmpty()) {
            client.eventBus.postEvent(MessageFetchEvent(messages))
        }
        if (gotBeginning) {
            channel.messages.gotBeginning = true
        }
        return messages
    }
}