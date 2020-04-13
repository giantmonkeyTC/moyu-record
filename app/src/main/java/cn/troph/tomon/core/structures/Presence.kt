package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

class Presence(client: Client, data: JsonObject) : Base(client, data) {

    val userId: String =
        if (data.has("user_id")) data["user_id"].asString else data["user"].asJsonObject["id"].asString
    var status: String = ""
        private set

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("status")) {
            status = data["status"].asString
        }
    }

    val user get() = client.users.get(userId)
}