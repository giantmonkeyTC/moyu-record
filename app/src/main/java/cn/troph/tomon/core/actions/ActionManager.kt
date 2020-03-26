package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.User

class ActionManager(val client: Client) {

    fun userUpdate(data: Any): User? = UserUpdateAction(client).handle(data)
    fun userLogin(data: Any): User? = UserLoginAction(client).handle(data)
}