package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

open class User(client: Client, data: JsonObject) : Base(client, data) {
    var id: String = ""
        protected set
    var username: String = ""
        protected set
    var discriminator: String = ""
        protected set
    var name: String = ""
        protected set
    var avatar: String = ""
        protected set
    var avatarURL: String = ""
        protected set

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("username")) {
            username = data["username"].asString
        }
        if (data.has("discriminator")) {
            discriminator = data["discriminator"].asString
        }
        if (data.has("name")) {
            name = data["name"].asString
        }
        if (data.has("avatar")) {
            avatar = data["avatar"].asString
        }
        if (data.has("avatarURL")) {
            avatarURL = data["avatarURL"].asString
        }
    }

    val identifier: String get() = "$username#$discriminator"

}