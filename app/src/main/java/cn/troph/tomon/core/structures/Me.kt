package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.network.services.AuthService
import cn.troph.tomon.core.utils.Validator
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class Me(client: Client, data: JsonData) : User(client, data) {
    constructor(client: Client) : this(client, mapOf())

    var email: String? = null
    var emailVerified: Boolean = false
    var phone: String? = null
    var phoneVerified: Boolean = false
    var token: String? = null

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("token")) {
            token = data["token"] as? String
        }
        if (data.contains("email")) {
            email = data["email"] as? String
        }
        if (data.contains("email_verified")) {
            emailVerified = data["email_verified"] as? Boolean ?: false
        }
        if (data.contains("phone")) {
            phone = data["phone"] as? String
        }
        if (data.contains("phone_verified")) {
            phoneVerified = data["phone_verified"] as? Boolean ?: false
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

    //TODO LOGIN, REGISTER, ETC.

    fun login(
        emailOrPhone: String? = null,
        password: String? = null,
        token: String? = null
    ): Observable<User?> {
        var request = when {
            token != null -> AuthService.LoginRequest(token = token)
            Validator.isEmail(emailOrPhone) -> AuthService.LoginRequest(
                email = emailOrPhone,
                password = password
            )
            Validator.isPhone(emailOrPhone) -> AuthService.LoginRequest(
                phone = emailOrPhone,
                password = password
            )
            else -> null
        }
        if (request != null) {
            return client.rest.authService.login(request).subscribeOn(Schedulers.io()).map {
                return@map client.actions.userLogin(it)
            }
        }
        return Observable.error(Exception("invalid parameter"))
    }

}