package cn.troph.tomon.ui.states

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.SocketClientState
import com.orhanobut.logger.Logger

class ApplicationObserver : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        //Logger.d("app start")
//        if (Client.global.socket.state != SocketClientState.OPEN || Client.global.socket.state != SocketClientState.CONNECTING) {
//            Client.global.socket.open()
//        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Logger.d("app stop")
        Client.global.socket.close()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {

    }
}