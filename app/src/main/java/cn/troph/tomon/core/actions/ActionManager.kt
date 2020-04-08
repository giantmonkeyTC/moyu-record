package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.User

class ActionManager(val client: Client) {

    fun userLogin(data: JsonData): User? = UserLoginAction(client).handle(data)
    fun userLogout(): Unit? = UserLogoutAction(client).handle(null)
    fun userUpdate(data: JsonData): User? = UserUpdateAction(client).handle(data)

    fun guildFetch(data: JsonArray): List<Guild>? = GuildFetchAction(client).handle(data)
    fun guildCreate(data: JsonData): Guild? = GuildCreateAction(client).handle(data)
    fun guildDelete(data: JsonData): Guild? = GuildDeleteAction(client).handle(data)
    fun guildUpdate(data: JsonData): Guild? = GuildUpdateAction(client).handle(data)


}