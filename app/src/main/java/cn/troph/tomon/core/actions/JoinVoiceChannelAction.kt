package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.VoiceAllowConnectEvent
import cn.troph.tomon.core.events.VoiceLeaveChannelEvent
import cn.troph.tomon.core.structures.VoiceAllowConnectReceive
import com.google.gson.Gson
import com.google.gson.JsonElement


class JoinVoiceChannelAction(client: Client) : Action<VoiceAllowConnectReceive>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): VoiceAllowConnectReceive? {
        super.handle(data, *extras)
        val obj = Gson().fromJson(data, VoiceAllowConnectReceive::class.java)
        if (obj.channelId != null) {
            Client.global.eventBus.postEvent(VoiceAllowConnectEvent(obj))
        } else {//leave channel event
            Client.global.eventBus.postEvent(VoiceLeaveChannelEvent(obj))
        }
        return obj
    }

}