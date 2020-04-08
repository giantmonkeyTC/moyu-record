package cn.troph.tomon.core.network.socket

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.network.Configs
import cn.troph.tomon.core.network.socket.handlers.handleGuildCreate
import cn.troph.tomon.core.network.socket.handlers.handleIdentity
import cn.troph.tomon.core.utils.Converter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import kotlin.concurrent.schedule

typealias Handler = (client: Client, packet: JsonData) -> Unit

enum class GatewayOp(val value: Int) {
    DISPATCH(0),
    HEARTBEAT(1),
    IDENTITY(2),
    HELLO(3),
    HEARTBEAT_ACK(4);

    companion object {
        private val map = GatewayOp.values().associateBy(GatewayOp::value)
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
        "GUILD_CREATE" to handleGuildCreate
    )

    constructor(client: Client) {
        _client = client
        Observable.create(_socketClient).observeOn(Schedulers.io()).subscribe(this)
    }

    fun open() {
        _socketClient.open(Configs.wss)
    }

    fun close(code: Int = 1000, reason: String? = null) {
        _socketClient.close()
    }

    fun send(op: GatewayOp, d: JsonData? = null) {
        if (d == null) {
            _socketClient.send(mapOf("op" to op.value))
        } else {
            _socketClient.send(mapOf("op" to op.value, "d" to d))
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

    private fun handleMessage(data: JsonData) {
        val intOp = Converter.toInt(data["op"])
        when (val op = GatewayOp.fromInt(intOp)) {
            GatewayOp.DISPATCH -> {
                val event = data["e"] as String
                val handler = _handlers[event]
                if (handler != null) {
                    handler(_client, data)
                }
            }
            GatewayOp.IDENTITY -> {
                val handler = _handlers["IDENTITY"]
                if (handler != null) {
                    handler(_client, data)
                }
            }
            GatewayOp.HELLO -> {
                val d = data["d"] as? JsonData
                _heartbeatInterval = Converter.toLong(d!!["heartbeat_interval"])
                _sessionId = d!!["session_id"] as String
                heartbeat()
                send(GatewayOp.IDENTITY, mapOf(
                    "token" to _client.me.token
                ))
            }
            GatewayOp.HEARTBEAT -> {
                send(GatewayOp.HEARTBEAT_ACK)
            }
            GatewayOp.HEARTBEAT_ACK -> {
            }
            else -> {
            }
        }
    }

}