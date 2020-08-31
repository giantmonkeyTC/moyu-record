package cn.troph.tomon.ui.states

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.GatewayOp
import cn.troph.tomon.core.network.socket.SocketClientState
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.TextChannel
import com.google.gson.Gson
import com.orhanobut.logger.Logger

class ApplicationObserver : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (Client.global.isBackground && Client.global.socket.state == SocketClientState.CLOSED) {
            Client.global.cacheChannelMap.clear()
            Client.global.channels.forEach {
                if (it is TextChannel)
                    Client.global.cacheChannelMap[it.id] = it.lastMessageId ?: ""
                else if (it is DmChannel)
                    Client.global.cacheChannelMap[it.id] = it.lastMessageId ?: ""
            }
            Logger.d("App in foreground")
            Client.global.socket.open()
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Client.global.isBackground = true
        Logger.d("App in background")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {

    }
}