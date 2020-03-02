package cn.troph.tomon.core.structures

import cn.troph.tomon.Client

class User(client: Client, data: Map<String, Any>) : Base(client, data) {
    var username: String = ""
    var discriminator: String = ""
    var name: String = ""
    var avatar: String = ""
    var avatarURL: String = ""

    override fun patch(data: Map<String, Any>) {
        super.patch(data)
        if (data.contains("username")) {
            this.username = data["username"] as String
        }
        if (data.contains("discriminator")) {
            this.discriminator = data["discriminator"] as String
        }
        if (data.contains("name")) {
            this.name = data["name"] as String
        }
        if (data.contains("avatar")) {
            this.avatar = data["avatar"] as String
        }
        if (data.contains("avatarURL")) {
            this.avatarURL = data["avatarURL"] as String
        }
    }

    val identifier: String get() = "$username#$discriminator"
}