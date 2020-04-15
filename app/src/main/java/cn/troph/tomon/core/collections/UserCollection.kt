package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.User
import com.google.gson.JsonObject

class UserCollection(client: Client) :
    BaseCollection<User>(client) {
    override fun instantiate(data: JsonObject): User? {
        return User(client, data)
    }
}