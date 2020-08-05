package cn.troph.tomon.core.network.socket

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import okhttp3.*
import java.lang.Error
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

enum class SocketClientState(val value: Int) {
    CONNECTING(0),
    OPEN(1),
    CLOSING(2),
    CLOSED(3),
}

enum class SocketEventType {
    CONNECTING,
    OPEN,
    CLOSING,
    CLOSED,
    SEND,
    RECEIVE,
    ERROR,
}

data class SocketEvent(
    val type: SocketEventType,
    val data: JsonElement? = null,
    val code: Int = 0,
    val reason: String? = null,
    val error: Throwable? = null
)

fun retryDelay(times: Int): Long {
    return when (times) {
        0 -> 500
        1 -> 1000
        2 -> 3000
        3, 4, 5 -> 5000
        else -> 10000
    }
}

class SocketClient : WebSocketListener(),
    ObservableOnSubscribe<SocketEvent> {

    private var _webSocket: WebSocket? = null
    private var _url: String? = null
    private var _retryCount = 0
    private var _reconnecting = false
    private var _state: SocketClientState = SocketClientState.CLOSED
    private val _timer = Timer()
    private var _timerTask: TimerTask? = null
    private var _emitter: ObservableEmitter<SocketEvent>? = null

    override fun subscribe(emitter: ObservableEmitter<SocketEvent>?) {
        this._emitter = emitter
    }

    fun open(url: String) {
        if (_state == SocketClientState.CLOSED) {
            println("[ws] opening socket")
            connect(url)
        }
    }

    fun close(code: Int = 1000, reason: String? = null) {
        _webSocket?.close(code, reason)
        _webSocket = null
        _state = SocketClientState.CLOSED
        _emitter?.onNext(SocketEvent(SocketEventType.CLOSED))
        println("[ws] close:${reason}")
    }

    fun send(data: JsonElement) {
        //Logger.d("Sending: ${data}")
        _webSocket?.send(Gson().toJson(data))
        _emitter?.onNext(SocketEvent(SocketEventType.SEND, data = data))
    }

    val url get() = _url

    val state get() = _state

    val reconnecting get() = _reconnecting

    val retryCount get() = _retryCount

    private fun connect(url: String) {
        _state = SocketClientState.CONNECTING
        _url = url
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient.Builder()
        client.callTimeout(0, TimeUnit.MILLISECONDS)
        _webSocket = client.build().newWebSocket(request, this)
        _emitter?.onNext(SocketEvent(SocketEventType.CONNECTING))
    }

    private fun reconnect() {
        if (_url == null) {
            return
        }
        _timerTask?.cancel()
        _reconnecting = true
        _timer.schedule(retryDelay(_retryCount)) {
            _retryCount += 1
            connect(_url!!)
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        println("[ws] open")
        _state = SocketClientState.OPEN
        _retryCount = 0
        _reconnecting = false
        _emitter?.onNext(SocketEvent(SocketEventType.OPEN))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        println("[ws] closing")
        _state = SocketClientState.CLOSING
        _emitter?.onNext(SocketEvent(SocketEventType.CLOSING))
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        println("[ws] closed")
        _state = SocketClientState.CLOSED
        when (code) {
            1006 -> if (url != null) reconnect()
        }
        _emitter?.onNext(SocketEvent(SocketEventType.CLOSED, code = code, reason = reason))
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Logger.d("Receive ${url}: "+text)
        try {
            val data = Gson().fromJson(text, JsonElement::class.java)
            _emitter?.onNext(SocketEvent(SocketEventType.RECEIVE, data = data))
        } catch (e: Exception) {
            _emitter?.onNext(SocketEvent(SocketEventType.ERROR, error = e))
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        _emitter?.onNext(SocketEvent(SocketEventType.ERROR, error = t))
    }

}