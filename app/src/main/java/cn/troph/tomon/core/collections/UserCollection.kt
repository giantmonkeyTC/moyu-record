package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.User

class UserCollection(client: Client, m: Map<String, User>? = null) :
    BaseCollection<User>(client, m) {
    override fun instantiate(data: Map<String, Any>): User? {
        return User(client, data)
    }
}