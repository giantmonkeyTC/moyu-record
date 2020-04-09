package cn.troph.tomon.core

import cn.troph.tomon.core.actions.ActionManager
import cn.troph.tomon.core.collections.ChannelCollection
import cn.troph.tomon.core.collections.GuildCollection
import cn.troph.tomon.core.collections.UserCollection
import cn.troph.tomon.core.network.Restful
import cn.troph.tomon.core.network.socket.Socket
import cn.troph.tomon.core.structures.Me
import cn.troph.tomon.core.utils.event.RxBus
import io.reactivex.rxjava3.core.Observable

class Client {

    val rest = Restful()
    val actions = ActionManager(this)
    val socket = Socket(this)
    val eventBus = RxBus()

    val me = Me(this)
    val users = UserCollection(this)
    val guilds = GuildCollection(this)
    val channels = ChannelCollection(this)

    val token get() = me.token ?: ""

    fun login(
        emailOrPhone: String? = null,
        password: String? = null,
        token: String? = null
    ): Observable<Void> {
        return me.login(emailOrPhone = emailOrPhone, password = password, token = token).flatMap { _ ->
            socket.open()
            return@flatMap Observable.empty<Void>()
        }
    }

}