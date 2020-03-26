package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.User

class UserUpdateAction(client: Client) : Action<User>(client) {

    override fun handle(data: Any): User? {
        val obj = data as JsonData
        var user = client.users.get(obj["id"] as String)
        if (user != null) {
            if (user.id == client.me.id) {
                client.me.patch(data)
            }
            user.patch(data)
        } else {
            user = client.users.add(data)
        }
        return user
    }
}