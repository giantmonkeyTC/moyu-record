package cn.troph.tomon

import android.app.Application
import cn.troph.tomon.core.Client
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class TomonApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        Client.global.initialize(this)
        EmojiManager.install(IosEmojiProvider())
    }
}