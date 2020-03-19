package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class Me(client: Client, data: JsonData) : User(client, data) {
    constructor(client: Client): this(client, mapOf())

    var email: String? = null
    var emailVerified: Boolean = false
    var phone: String? = null
    var phoneVerified: Boolean = false
    var token: String? = null

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("email")) {
            email = data["email"] as String
        }
        if (data.contains("email_verified")) {
            emailVerified = data["email"] as Boolean
        }
        if (data.contains("phone")) {
            phone = data["phone"] as String
        }
        if (data.contains("phone_verified")) {
            phoneVerified = data["phone_verified"] as Boolean
        }
    }

    fun clear() {
        id = ""
        username = ""
        name = ""
        discriminator = ""
        avatar = ""
        avatarURL = ""
        email = null
        emailVerified = false
        phone = null
        phoneVerified = false
        token = null
    }
}