package cn.troph.tomon.core.network.socket

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.VoiceSocketStateEvent
import cn.troph.tomon.core.network.NetworkConfigs
import cn.troph.tomon.core.network.socket.handlers.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*


class VoiceSocket : Observer<SocketEvent> {
    private val _client: Client

    constructor(client: Client) {
        _client = client
        Observable.create(_socketClient).observeOn(Schedulers.io()).subscribe(this)
    }

    private val _handlers = mapOf(
        "SPEAKING" to handleVoiceSpeak
    )

    private val _socketClient: SocketClient = SocketClient()
    private var timer = Timer()

    fun open() {
        _socketClient.open(NetworkConfigs.wssVoice)
    }

    fun close() {
        stopHeartBeat()
        _socketClient.close()
    }

    fun send(op: GatewayOp, d: JsonElement) {
        _socketClient.send(Gson().toJsonTree(mapOf("op" to op.value, "d" to d)))
    }

    fun sendHeartBeat() {
        val timerTask = object : TimerTask() {
            override fun run() {
                send(GatewayOp.HEARTBEAT, Gson().toJsonTree(""))
            }
        }
        timer = Timer()
        timer.scheduleAtFixedRate(timerTask, 0, 5000)
    }

    fun stopHeartBeat() {
        timer.cancel()
    }

    private fun handleMessage(data: JsonElement) {
        if (data.isJsonArray) {
            return
        }
        val obj = data.asJsonObject
        when (obj["op"].asInt) {
            2 -> {
                sendHeartBeat()
            }

            5 -> {//鉴权成功
            }

            3 -> {
                //heart beat ack ignore
            }

            6 -> {//someone is speaking
                val handler = _handlers["SPEAKING"]
                handler?.let {
                    it(_client, obj)
                }
            }
        }
    }

    override fun onComplete() {

    }

    override fun onSubscribe(d: Disposable?) {

    }

    override fun onNext(t: SocketEvent?) {
        when (t?.type) {
            SocketEventType.RECEIVE -> handleMessage(t.data!!)

            SocketEventType.OPEN -> {
                _client.eventBus.postEvent(VoiceSocketStateEvent(true))
            }
            SocketEventType.CLOSED -> {
                _client.eventBus.postEvent(VoiceSocketStateEvent(false))
            }
        }
    }

    override fun onError(e: Throwable?) {

    }
}