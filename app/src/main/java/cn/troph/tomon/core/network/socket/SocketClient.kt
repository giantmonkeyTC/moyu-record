package cn.troph.tomon.core.network.socket

import cn.troph.tomon.core.JsonData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.util.*
import kotlin.concurrent.schedule

enum class SocketClientState(val value: Int) {
    CONNECTING(0),
    OPEN(1),
    CLOSING(2),
    CLOSED(3),
}

fun retryDelay(times: Int): Long {
    return when (times) {
        0 -> 500
        1 -> 1000
        2 -> 3000
        3, 4, 5 -> 5000
        else -> 10000
    }
}

interface SocketClientListener {
    fun onOpen() {}
    fun onClose(code: Int, reason: String?) {}
    fun onMessage(data: JsonData) {}
    fun onError(text: String) {}
    fun onConnecting() {}
    fun onFailure(t: Throwable) {}
}

class SocketClient(private val listener: SocketClientListener) : WebSocketListener() {

    private var _webSocket: WebSocket? = null
    private var _url: String? = null
    private var _retryCount = 0
    private var _reconnecting = false
    private var _state: SocketClientState = SocketClientState.CLOSED
    private val _timer = Timer()
    private var _timerTask: TimerTask? = null

    fun open(url: String) {
        if (_state == SocketClientState.CLOSED) {
            connect(url);
        }
    }

    fun close(code: Int = 1000, reason: String? = null) {
        _webSocket?.close(code, reason)
    }

    fun send(data: JsonData) {
        _webSocket?.send(Gson().toJson(data))
    }

    val url get() = _url

    val state get() = _state

    val reconnecting get() = _reconnecting

    val retryCount get() = _retryCount

    private fun connect(url: String) {
        _state = SocketClientState.CONNECTING
        _url = url
        val request = Request.Builder().url(url).build()
        _webSocket = OkHttpClient().newWebSocket(request, this)
        listener?.onConnecting()
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
        listener?.onOpen()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        println("[ws] closing")
        _state = SocketClientState.CLOSING
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        println("[ws] close")
        _state = SocketClientState.CLOSED
        when (code) {
            1006 -> if (url != null) reconnect()
        }
        listener?.onClose(code, reason)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        try {
            val mapType = object : TypeToken<JsonData>() {}.type
            val data = Gson().fromJson<JsonData>(text, mapType)
            listener?.onMessage(data)
        } catch (e: Exception) {
            listener?.onError(text)
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        listener?.onFailure(t)
    }

}