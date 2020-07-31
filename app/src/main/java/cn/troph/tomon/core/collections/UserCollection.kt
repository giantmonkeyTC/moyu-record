package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.User
import com.google.gson.JsonObject

class UserCollection(client: Client) :
    BaseCollection<User>(client) {
    override fun instantiate(data: JsonObject): User? {
        return User(client, data)
    }

    fun findWithIdentifier(identifier: String):User? {
        val result = this.filter { user ->
            user.identifier == identifier
        }
        return if (result.length>0) result.values.first() else null
    }
}