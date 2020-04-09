package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.events.UserRegisterEvent
import cn.troph.tomon.core.structures.User

class UserRegisterAction(client: Client) : Action<User>(client) {
    override fun handle(data: Any, extra: Any?): User? {
        val user = client.users.add(data as JsonData)
        client.me.patch(data)
        //TODO SAVE TOKEN
        if (user != null)
            client.eventBus.postEvent(UserRegisterEvent(client.me))
        return null
    }
}