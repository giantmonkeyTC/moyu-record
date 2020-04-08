package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.events.UserFetchEvent
import cn.troph.tomon.core.structures.User

class UserFetchAction(client: Client) : Action<User>(client) {
    override fun handle(data: Any, extra: Any?): User? {
        val user = client.users.add(data as JsonData)
        if (user != null) {
            if (user.id == client.me.id)
                client.me.patch(data)
        }
        client.eventBus.postEvent(UserFetchEvent(user))
        return user
    }
}