package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.Assets
import cn.troph.tomon.core.utils.optBoolean
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonNull
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

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("name")) {
            name = if (data["name"] is JsonNull) {
                "emoji"
            } else if (data["name"].asString == "") {
                "emoji"
            } else
                data["name"].asString
        }
        if (data.has("user")) {
            val user = client.users.add(data["user"].asJsonObject)
            userId = user?.id
        }
        if (data.has("animated")) {
            animated = data["animated"].optBoolean ?: false
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    val url get() = Assets.emojiURL(id, animated)

    val user get() = client.users[userId ?: ""]

}