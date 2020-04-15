package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildEmoji
import com.google.gson.JsonElement

class EmojiFetchAction(client: Client) : Action<List<GuildEmoji>>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): List<GuildEmoji>? {
        return super.handle(data, *extras)
    }
}