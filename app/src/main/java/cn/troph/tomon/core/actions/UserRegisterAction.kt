package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.User
import com.google.gson.JsonElement

class UserRegisterAction(client: Client) :Action<User>(client){
    override fun handle(data: JsonElement?, vararg extras: Any?): User? {
        return super.handle(data, *extras)
    }
}