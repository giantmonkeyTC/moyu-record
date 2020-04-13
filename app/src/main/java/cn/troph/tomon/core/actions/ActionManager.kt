package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.User
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class ActionManager(val client: Client) {

    fun userLogin(data: JsonObject): User? = UserLoginAction(client).handle(data)
    fun userLogout(): Unit? = UserLogoutAction(client).handle(null)
    fun userUpdate(data: JsonObject): User? = UserUpdateAction(client).handle(data)

    fun guildFetch(data: JsonArray): List<Guild>? = GuildFetchAction(client).handle(data)
    fun guildCreate(data: JsonObject): Guild? = GuildCreateAction(client).handle(data)
    fun guildDelete(data: JsonObject): Guild? = GuildDeleteAction(client).handle(data)
    fun guildUpdate(data: JsonObject): Guild? = GuildUpdateAction(client).handle(data)


}