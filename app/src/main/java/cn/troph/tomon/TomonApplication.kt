package cn.troph.tomon

import android.app.Application
import cn.troph.tomon.core.Client
import com.downloader.PRDownloader
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class TomonApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        PRDownloader.initialize(this)
        Client.global.initialize(this)
    }
}