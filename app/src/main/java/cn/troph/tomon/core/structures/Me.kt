package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.AuthService
import cn.troph.tomon.core.utils.Validator
import cn.troph.tomon.core.utils.optBoolean
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class Me(client: Client, data: JsonObject = JsonObject()) : User(client, data) {

    var email: String? = null
        private set
    var emailVerified: Boolean = false
        private set
    var phone: String? = null
        private set
    var phoneVerified: Boolean = false
        private set
    var token: String? = null
        private set

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("token")) {
            token = data["token"].optString
        }
        if (data.has("email")) {
            email = data["email"].optString
        }
        if (data.has("email_verified")) {
            emailVerified = data["email_verified"].optBoolean ?: false
        }
        if (data.has("phone")) {
            phone = data["phone"].optString
        }
        if (data.has("phone_verified")) {
            phoneVerified = data["phone_verified"].optBoolean ?: false
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
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