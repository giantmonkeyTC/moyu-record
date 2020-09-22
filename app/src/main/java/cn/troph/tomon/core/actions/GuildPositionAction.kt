package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildPositionEvent
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

class GuildPositionAction(client: Client) : Action<Unit>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): Unit? {
        val list = GsonBuilder().create()
            .fromJson(data?.asJsonObject?.get("positions"), Array<Position>::class.java)
            .toMutableList()
        client.eventBus.postEvent(GuildPositionEvent(list))
        return Unit
    }
}