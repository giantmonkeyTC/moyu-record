package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.UserUpdateEvent
import cn.troph.tomon.core.structures.User
import com.google.gson.JsonElement

class UserUpdateAction(client: Client) : Action<User>(client) {

    override fun handle(data: JsonElement?, extra: Any?): User? {
        val obj = data!!.asJsonObject
        var user = client.users.get(obj["id"].asString)
        if (user != null) {
            if (user.id == client.me.id) {
                client.me.update(obj)
            }
            user.update(obj)
        } else {
            user = client.users.add(obj)
        }
        if (user != null) {
            client.eventBus.postEvent(UserUpdateEvent(user))
        }
        return user
    }
}