package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.UserLogoutEvent
import cn.troph.tomon.core.structures.User

class UserLogoutAction(client: Client) : Action<User>(client) {
    override fun handle(data: Any, extra: Any?): User? {
        client.users.clear()
        client.guilds.clear()
        client.channels.clear()
        client.me.clear()
        //TODO CLEAR TOKEN
        client.eventBus.postEvent(UserLogoutEvent())
        return null
    }
}