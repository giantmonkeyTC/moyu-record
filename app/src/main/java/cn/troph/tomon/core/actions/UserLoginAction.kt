package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.UserLoginEvent
import cn.troph.tomon.core.structures.User
import com.google.gson.JsonElement

class UserLoginAction(client: Client) : Action<User>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): User? {
        val obj = data!!.asJsonObject
        val user = client.users.add(obj)
        // TODO token
        client.me.update(obj)
        if (user != null) {
            client.eventBus.postEvent(UserLoginEvent())
        }
        return user
    }
}