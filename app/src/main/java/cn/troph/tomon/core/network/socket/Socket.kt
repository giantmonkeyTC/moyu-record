package cn.troph.tomon.core.network.socket

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.Configs
import cn.troph.tomon.core.network.socket.handlers.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import kotlin.concurrent.schedule

typealias Handler = (client: Client, packet: JsonObject) -> Unit

enum class GatewayOp(val value: Int) {
    DISPATCH(0),
    HEARTBEAT(1),
    IDENTITY(2),
    HELLO(3),
    HEARTBEAT_ACK(4),
    VOICE(5);

    companion object {
        private val map = values().associateBy(GatewayOp::value)
        fun fromInt(type: Int) = map[type]
    }
}

class Socket : Observer<SocketEvent> {

    private val _socketClient: SocketClient = SocketClient()
    private val _client: Client
    private var _ready: Boolean = false
    private var _sessionId: String? = null
    private var _heartbeatTimer = Timer()
    private var _heartbeatTimerTask: TimerTask? = null
    private var _heartbeatInterval: Long = 40000
    private val _handlers = mapOf(
        "IDENTITY" to handleIdentity,
        "GUILD_CREATE" to handleGuildCreate,
        "GUILD_DELETE" to handleGuildDelete,
        "GUILD_UPDATE" to handleGuildUpdate,
        "CHANNEL_CREATE" to handleChannelCreate,
        "CHANNEL_DELETE" to handleChannelDelete,
        "CHANNEL_UPDATE" to handleChannelUpdate,
        "USER_GUILD_SETTINGS" to handleGuildSettings,
        "MESSAGE_CREATE" to handleMessageCreate,
        "MESSAGE_DELETE" to handleMessageDelete,
        "MESSAGE_UPDATE" to handleMessageUpdate,
        "MESSAGE_REACTION_ADD" to handleMessageReactionAdd,
        "MESSAGE_REACTION_REMOVE" to handleMessageReactionRemove,
        "GUILD_POSITION" to handleGuildPosition,
        "USER_PRESENCE_UPDATE" to handlePresenceUpdate,
        "VOICE_ALLOW_CONNECT" to handleVoiceConnectAllow
    )

    constructor(client: Client) {
        _client = client
        Observable.create(_socketClient).observeOn(Schedulers.io()).subscribe(this)
    }

    fun open() {
        _socketClient.open(Configs.wss)
    }

    fun close(code: Int = 1000, reason: String? = null) {
        stopHeartbeat()
        _socketClient.close()
    }

    fun send(op: GatewayOp, d: JsonElement? = null) {
        if (d == null) {
            _socketClient.send(Gson().toJsonTree(mapOf("op" to op.value)))
        } else {
            _socketClient.send(Gson().toJsonTree(mapOf("op" to op.value, "d" to d)))
        }
    }

    val state get() = _socketClient.state

    private fun heartbeat() {
        println("heartbeat")
        send(GatewayOp.HEARTBEAT)
        _heartbeatTimerTask = _heartbeatTimer.schedule(_heartbeatInterval) {
            heartbeat()
        }
    }

    private fun stopHeartbeat() {
        _heartbeatTimerTask?.cancel()
        _heartbeatTimerTask = null
    }

    override fun onSubscribe(d: Disposable?) {

    }

    override fun onNext(t: SocketEvent?) {
        when (t?.type) {
            SocketEventType.RECEIVE -> handleMessage(t.data!!)
        }
    }

    override fun onError(e: Throwable?) {

    }

    override fun onComplete() {

    }

    private fun handleMessage(data: JsonElement) {
        if (data.isJsonArray) {
            return
        }
        val obj = data.asJsonObject
        val intOp = obj["op"].asInt
        when (val op = GatewayOp.fromInt(intOp)) {
            GatewayOp.DISPATCH -> {
                val event = obj["e"].asString
                val handler = _handlers[event]
                if (handler != null) {
                    handler(_client, obj)
                }
            }
            GatewayOp.IDENTITY -> {
                val handler = _handlers["IDENTITY"]
                if (handler != null) {
                    handler(_client, obj)
                }
            }
            GatewayOp.HELLO -> {
                val d = obj["d"].asJsonObject
                println(d)
                _heartbeatInterval = d["heartbeat_interval"].asLong
                _sessionId = d["session_id"].asString
                heartbeat()
                send(
                    GatewayOp.IDENTITY, Gson().toJsonTree(
                        mapOf(
                            "token" to _client.me.token
                        )
                    )
                )
            }
            GatewayOp.HEARTBEAT -> {
                send(GatewayOp.HEARTBEAT_ACK)
            }
            GatewayOp.HEARTBEAT_ACK -> {

            }
            GatewayOp.VOICE -> {
                val handler = _handlers["VOICE_ALLOW_CONNECT"]
                handler?.let {
                    it(_client, obj)
                }
            }
            else -> {
            }
        }
    }

}