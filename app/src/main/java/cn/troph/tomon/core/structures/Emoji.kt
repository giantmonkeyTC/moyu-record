package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.Assets
import com.google.gson.JsonObject

open class Emoji(client: Client, data: JsonObject) : Base(client, data) {

    var id: String = ""
        private set
    var name: String = ""
        private set
    var userId: String? = null
        private set
    var animated: Boolean = false
        private set

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("name")) {
            name = data["name"].asString
        }
        if (data.has("user")) {
            val user = client.users.add(data["user"].asJsonObject)
            userId = user?.id
        }
    }

    val url get() = Assets.emojiURL(id, animated)

    val user get() = client.users.get(userId ?: "")

}