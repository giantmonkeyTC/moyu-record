package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelAckEvent
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.core.utils.snowflake
import com.google.gson.JsonElement

class ChannelAckAction(client: Client) : Action<Unit>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?) {
        val channelId = extras[0] as? String
        val id = extras[1] as? String
        if (channelId == null || id == null) {
            return
        }
        val channel = client.channels[channelId]
        if (channel is TextChannelBase) {
            val curAck = channel.ackMessageId ?: "0";
            if (id.snowflake > curAck.snowflake) {
                channel.update(mapOf("ack_message_id" to id))
                client.eventBus.postEvent(ChannelAckEvent(channel))
            }
        }
    }
}