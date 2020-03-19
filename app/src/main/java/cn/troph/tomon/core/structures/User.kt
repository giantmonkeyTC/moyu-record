package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

open class User(client: Client, data: JsonData) : Base(client, data) {
    var id: String = ""
    var username: String = ""
    var discriminator: String = ""
    var name: String = ""
    var avatar: String = ""
    var avatarURL: String = ""

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("id")) {
            id = data["id"] as String
        }
        if (data.contains("username")) {
            username = data["username"] as String
        }
        if (data.contains("discriminator")) {
            discriminator = data["discriminator"] as String
        }
        if (data.contains("name")) {
            name = data["name"] as String
        }
        if (data.contains("avatar")) {
            avatar = data["avatar"] as String
        }
        if (data.contains("avatarURL")) {
            avatarURL = data["avatarURL"] as String
        }
    }

    val identifier: String get() = "$username#$discriminator"

}