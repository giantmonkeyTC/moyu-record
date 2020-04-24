package cn.troph.tomon.core

import android.app.Application
import android.content.SharedPreferences
import android.content.Context
import cn.troph.tomon.core.actions.ActionManager
import cn.troph.tomon.core.collections.*
import cn.troph.tomon.core.network.Restful
import cn.troph.tomon.core.network.socket.Socket
import cn.troph.tomon.core.structures.Me
import cn.troph.tomon.core.utils.event.RxBus
import io.reactivex.rxjava3.core.Observable

class Client {

    private object HOLDER {
        val INSTANCE = Client()
    }

    companion object {
        val global: Client by lazy { HOLDER.INSTANCE }
    }

    val rest = Restful()
    val actions = ActionManager(this)
    val socket = Socket(this)
    val eventBus = RxBus()
    lateinit var preferences: SharedPreferences
        private set

    val me = Me(this)
    val users = UserCollection(this)
    val guilds = GuildCollection(this)
    val channels = ChannelCollection(this)
    val dmChannels = DmChannelCollection(this)
    val emojis = EmojiCollection(this)
    val presences = PresenceCollection(this)
    val guildSettings = GuildSettingsCollection(this)

    val token get() = me.token
    val auth get() = "Bearer ${token ?: ""}"

    fun initialize(app: Application) {
        preferences = app.getSharedPreferences("tomon", Context.MODE_PRIVATE)
        val token = preferences.getString("token", null)
        if (token != null) {
            me.update("token", token)
        }
    }

    fun login(
        emailOrPhone: String? = null,
        password: String? = null
    ): Observable<Void> {
        return me.login(emailOrPhone = emailOrPhone, password = password, token = me.token)
            .flatMap { _ ->
                socket.open()
                return@flatMap Observable.empty<Void>()
            }
    }

}