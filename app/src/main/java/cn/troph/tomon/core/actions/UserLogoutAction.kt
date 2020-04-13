package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.UserLogoutEvent
import com.google.gson.JsonElement

class UserLogoutAction(client: Client) : Action<Unit>(client) {
    override fun handle(data: JsonElement?, extra: Any?): Unit? {
        client.users.clear()
        client.guilds.clear()
        client.channels.clear()
        client.me.clear()
        //TODO CLEAR TOKEN
        client.eventBus.postEvent(UserLogoutEvent())
        return null
    }
}