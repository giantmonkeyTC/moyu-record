package cn.troph.tomon

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import cn.troph.tomon.core.Client
import com.downloader.PRDownloader
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.sentry.android.core.SentryAndroid


class TomonApplication : Application() {

    private lateinit var sAnalytics:GoogleAnalytics
    private lateinit var sTracker : Tracker

    override fun onCreate() {
        super.onCreate()
        SentryAndroid.init(this)
        EmojiCompat.init(BundledEmojiCompatConfig(this))
        Logger.addLogAdapter(AndroidLogAdapter())
        PRDownloader.initialize(this)
        Client.global.initialize(this)
        sAnalytics = GoogleAnalytics.getInstance(this)
        sTracker = sAnalytics.newTracker(R.xml.global_tracker)
        sTracker.enableAutoActivityTracking(true)
    }

    @Synchronized
    fun getDefaultTracker(): Tracker{
        return sTracker
    }
}