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
        unionId: String? = null,
        password: String? = null,
        token: String? = null,
        code: String? = null
    ): Observable<User?> {
        val request = when {
            token?.isNotEmpty() == true -> AuthService.LoginRequest(token = token)
            Validator.isFullName(unionId) && password != null -> AuthService.LoginRequest(
                full_name = unionId,
                password = password
            )
            Validator.isEmail(unionId) && password != null -> AuthService.LoginRequest(
                email = unionId,
                password = password
            )
            Validator.isPhone(unionId) && password != null -> AuthService.LoginRequest(
                phone = unionId,
                password = password
            )
            Validator.isPhone(unionId) && code != null && password == null -> AuthService.LoginRequest(
                phone = unionId,
                code = code
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

    fun register(
        username: String? = null,
        unionId: String? = null,
        code: String? = null,
        password: String? = null,
        invite: String? = null
    ): Observable<User> {
        var request = when {
            Validator.isPhone(unionId) -> AuthService.RegisterRequest(
                username = username,
                phone = unionId,
                code = code,
                password = password,
                email = null,
                invite = invite
            )
            Validator.isEmail(unionId) -> AuthService.RegisterRequest(
                username = username,
                email = unionId,
                code = code,
                password = password,
                phone = null,
                invite = invite
            )
            unionId == null && username != null -> AuthService.RegisterRequest(
                username = username,
                phone = null,
                code = code,
                password = password,
                email = null,
                invite = invite
            )
            else -> null
        }
        if (request != null) {
            return client.rest.authService.register(request).subscribeOn(Schedulers.io()).map {
                return@map client.actions.userRegister(data = it)
            }
        }
        return Observable.error(Exception("invalid parameter"))
    }

    fun logout(): Unit? {
        return client.actions.userLogout()
    }

}