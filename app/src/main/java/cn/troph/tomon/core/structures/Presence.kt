package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.utils.BitField

class Presence(client: Client, data: JsonData) : Base(client, data) {
    var userId: String = ""
    var status: String = ""

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("user_id")) {
            userId = data["user_id"] as String
        }
        if (data.contains("user")) {
            userId = (data["user"] as Map<String, Any>)["id"] as String
        }
        if (data.contains("status")) {
            status = data["status"] as String
        }
    }
    val user get() = client.users.get(userId)
}