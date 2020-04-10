package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.utils.Assets

open class Emoji(client: Client, data: JsonData) : Base(client, data) {

    val id: String = data["id"] as String
    var name: String = ""
        private set
    var userId: String? = null
        private set
    var animated: Boolean = false
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("name")) {
            name = data["name"] as String
        }
        if (data.contains("user")) {
            val user = client.users.add(data["user"] as JsonData)
            userId = user?.id
        }
    }

    val url get() = Assets.emojiURL(id, animated)

    val user get() = client.users.get(userId ?: "")

}