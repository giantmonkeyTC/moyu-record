package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelAckEvent
import cn.troph.tomon.core.network.services.ChannelService
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.core.utils.optString
import cn.troph.tomon.core.utils.snowflake
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ChannelAckAction(client: Client) : Action<Unit>(client) {

    override fun handle(acks: JsonElement?, vararg extras: Any?): Unit? {
        if (acks == null) return null
        for (ack in acks.asJsonArray) {
            val channelId = ack.asJsonObject["channel_id"].optString
            val id = ack.asJsonObject["message_id"].optString
            if (channelId == null || id == null) {
                return null
            }
            val channel = client.channels[channelId]
            if (channel is TextChannelBase) {
                val curAck = channel.ackMessageId ?: "0";
                if (id.snowflake > curAck.snowflake) {
                    channel.update(mapOf("ack_message_id" to id))
                }
            }
            if (channel is TextChannel) {
                channel.mention = 0
            }
        }
        val acksJsonObject = JsonObject()
        acksJsonObject.add("acks", acks)
        var channels = Gson().fromJson(acksJsonObject, ChannelService.ChannelsBulkAcks::class.java)
        client.eventBus.postEvent(ChannelAckEvent(channels))
        return Unit
    }
}