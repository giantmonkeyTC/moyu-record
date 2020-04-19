package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

class Presence(client: Client, data: JsonObject) : Base(client, data) {

    var userId: String = ""
        private set
    var status: String = ""
        private set

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("user_id")) {
            userId = data["user_id"].asString
        } else if (data.has("user")) {
            val user = client.users.add(data["user"].asJsonObject)
            userId = user?.id ?: ""
        }
        if (data.has("status")) {
            status = data["status"].asString
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    val user get() = client.users.get(userId)
}