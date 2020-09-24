package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.VoiceAllowConnectEvent
import cn.troph.tomon.core.events.VoiceLeaveChannelEvent
import cn.troph.tomon.core.events.VoiceStateUpdateEvent
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.structures.VoiceChannel
import cn.troph.tomon.core.structures.VoiceConnectStateReceive
import cn.troph.tomon.core.structures.VoiceUpdate
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger


val handleVoiceStateHandler: Handler = { client, packet ->
    Logger.d(packet)
    val voiceStateUpdateEvent = VoiceStateUpdateEvent(
        Gson().fromJson(
            packet["d"].asJsonObject,
            VoiceUpdate::class.java
        )
    )

    if (voiceStateUpdateEvent.voiceUpdate.channelId.isEmpty()) {
        var toDelete:VoiceUpdate? = null
        var voiceChannel:VoiceChannel? = null
        for (channel in Client.global.channels) {
            if (channel is VoiceChannel) {
                toDelete = channel.voiceStates.find {
                    it.userId == voiceStateUpdateEvent.voiceUpdate.userId
                }
                if (toDelete != null) {
                    voiceChannel = channel
                    break
                }
            }
        }
        toDelete?.let {
            voiceChannel?.voiceStates?.remove(it)
            if (voiceChannel?.voiceStates?.size ==0) {
                voiceChannel.isJoined = false
            }
        }
    } else {
        for (channel in Client.global.channels) {
            if (channel is VoiceChannel) {
                val find = channel.voiceStates.find {
                    it.userId == voiceStateUpdateEvent.voiceUpdate.userId
                }
                channel.voiceStates.remove(find)
                if (channel.voiceStates.size ==0) {
                    channel.isJoined = false
                }
                if (channel.id == voiceStateUpdateEvent.voiceUpdate.channelId) {
                    channel.voiceStates.add(voiceStateUpdateEvent.voiceUpdate)
                    channel.isJoined = true
                }
            }
        }
    }

    client.eventBus.postEvent(voiceStateUpdateEvent)
}

val handleVoiceLeaveHandler: Handler = { client: Client, packet: JsonObject ->
    Client.global.eventBus.postEvent(
        VoiceLeaveChannelEvent(
            Gson().fromJson(
                packet.asJsonObject["d"].asJsonObject,
                VoiceConnectStateReceive::class.java
            )
        )
    )
}

val handleVoiceJoinHandler: Handler = { client: Client, packet: JsonObject ->
    Client.global.eventBus.postEvent(
        VoiceAllowConnectEvent(
            Gson().fromJson(
                packet.asJsonObject["d"].asJsonObject,
                VoiceConnectStateReceive::class.java
            )
        )
    )
}


