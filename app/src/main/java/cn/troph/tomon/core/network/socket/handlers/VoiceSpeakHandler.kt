package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.VoiceSpeakEvent
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.structures.Speaking
import com.google.gson.Gson
import com.google.gson.JsonObject

val handleVoiceSpeak: Handler = { client: Client, packet: JsonObject ->
    val data = packet["d"].asJsonObject
    client.eventBus.postEvent(VoiceSpeakEvent(Gson().fromJson(data, Speaking::class.java)))
}