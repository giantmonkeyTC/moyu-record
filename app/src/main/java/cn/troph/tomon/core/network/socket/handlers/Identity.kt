package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import com.google.gson.JsonElement

val handleIdentity: Handler = { client: Client, packet: JsonElement ->
//    val data = packet["d"] as JsonData
//
//    (data["guilds"] as JsonArray).forEach {guild ->
//        if (guild["system_channel_id"] == "0") {
//            guild["system_channel_id"] = null
//        }
//    }

    println(packet)
}