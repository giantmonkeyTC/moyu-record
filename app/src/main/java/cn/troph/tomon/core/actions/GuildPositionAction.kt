package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import com.google.gson.JsonElement

class GuildPositionAction(client: Client) : Action<Unit>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): Unit? {

        return super.handle(data, *extras)
    }
}