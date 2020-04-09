package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class Presence(client: Client, data: JsonData) : Base(client, data) {

    var userId: String = ""
        private set
    var status: String = ""
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("user_id")) {
            userId = data["user_id"] as String
        }
        if (data.contains("user")) {
            userId = (data["user"] as JsonData)["id"] as String
        }
        if (data.contains("status")) {
            status = data["status"] as String
        }
    }

    val user get() = client.users.get(userId)
}