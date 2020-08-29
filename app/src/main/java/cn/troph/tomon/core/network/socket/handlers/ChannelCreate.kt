package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonNull

val handleChannelCreate: Handler = { client: Client, packet: JsonElement ->
    val c = packet.asJsonObject["d"].asJsonObject
    c["parent_id"]?.let {
        if (it.optString == "0") {
            c.add("parent_id", JsonNull.INSTANCE)
        }
    }
    client.actions.channelCreate(c)
}