package cn.troph.tomon

import android.app.Application
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import cn.troph.tomon.core.Client
import com.downloader.PRDownloader
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class TomonApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        EmojiCompat.init(BundledEmojiCompatConfig(this))
        Logger.addLogAdapter(AndroidLogAdapter())
        PRDownloader.initialize(this)
        Client.global.initialize(this)
    }
}