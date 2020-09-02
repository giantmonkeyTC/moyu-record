package cn.troph.tomon.core

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import cn.troph.tomon.core.actions.ActionManager
import cn.troph.tomon.core.collections.*
import cn.troph.tomon.core.network.Restful
import cn.troph.tomon.core.network.socket.Socket
import cn.troph.tomon.core.network.socket.SocketClientState
import cn.troph.tomon.core.network.socket.VoiceSocket
import cn.troph.tomon.core.structures.Me
import cn.troph.tomon.core.structures.StampPack
import cn.troph.tomon.core.structures.User
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
    val voiceSocket = VoiceSocket(this)
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
    val stamps: MutableList<StampPack> = mutableListOf()

    val token get() = me.token
    val auth get() = "Bearer ${token ?: ""}"
    val loggedIn get() = socket.state == SocketClientState.OPEN

    fun initialize(app: Application) {
        preferences = app.getSharedPreferences("tomon", Context.MODE_PRIVATE)
        val token = preferences.getString("token", null)
        if (token != null) {
            me.update("token", token)
        }
    }

    fun login(
        unionId: String? = null,
        password: String? = null
    ): Observable<Unit> {
        return me.login(
            unionId = unionId,
            password = password,
            token = if (unionId != null && password != null) null else me.token
        )
            .map {
                socket.open()
            }
    }

    fun register(
        username: String? = null,
        unionId: String? = null,
        code: String? = null,
        password: String? = null,
        invite: String? = null
    ): Observable<User> {
        return me.register(
            unionId = unionId,
            password = password,
            code = code,
            invite = invite,
            username = username
        )
    }

    fun logout(): Unit? {
        return me.logout()
    }


}