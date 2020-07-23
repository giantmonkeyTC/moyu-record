package cn.troph.tomon

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import cn.troph.tomon.core.Client
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.downloader.PRDownloader
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.sentry.android.core.SentryAndroid


class TomonApplication : Application() {

    private lateinit var sAnalytics: GoogleAnalytics
    private lateinit var sTracker: Tracker

    @SuppressLint("MissingPermission")
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
        initPushService(this)
    }

    private fun initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // 通知渠道的id
            val id = "1"
            // 用户可以看到的通知渠道的名字.
            val name: CharSequence = "TOMON"
            // 用户可以看到的通知渠道的描述
            val description = "TOMON 消息推送"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(id, name, importance)
            // 配置通知渠道的属性
            mChannel.description = description
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true)
            mChannel.lightColor = Color.GREEN
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun initPushService(applicationContext: Context) {
        PushServiceFactory.init(applicationContext)
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.register(applicationContext, object : CommonCallback {
            override fun onSuccess(response: String) {
                Log.i("PUSH", "init cloudchannel success")
                initChannel()
            }

            override fun onFailed(errorCode: String, errorMessage: String) {
                Log.e(
                    "PUSH",
                    "init cloudchannel failed -- errorcode:$errorCode -- errorMessage:$errorMessage"
                )
            }
        })
    }

    @Synchronized
    fun getDefaultTracker(): Tracker {
        return sTracker
    }
}