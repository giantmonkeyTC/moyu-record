package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class Presence(client: Client, data: JsonData) : Base(client, data) {

    val userId: String =
        if (data.contains("user_id")) data["user_id"] as String else (data["user"] as JsonData)["id"] as String
    var status: String = ""
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("status")) {
            status = data["status"] as String
        }
    }

    val user get() = client.users.get(userId)
}