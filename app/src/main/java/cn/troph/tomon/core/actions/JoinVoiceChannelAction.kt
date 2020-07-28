package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.VoiceAllowConnectEvent
import cn.troph.tomon.core.structures.VoiceAllowConnect
import com.google.gson.Gson
import com.google.gson.JsonElement


class JoinVoiceChannelAction(client: Client) : Action<VoiceAllowConnect>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): VoiceAllowConnect? {
        super.handle(data, *extras)
        val obj = Gson().fromJson(data, VoiceAllowConnect::class.java)
        Client.global.eventBus.postEvent(VoiceAllowConnectEvent(obj))
        return obj
    }

}