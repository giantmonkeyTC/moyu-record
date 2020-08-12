package cn.troph.tomon

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.lifecycle.ProcessLifecycleOwner
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.states.ApplicationObserver
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.alibaba.sdk.android.push.register.MeizuRegister
import com.alibaba.sdk.android.push.register.MiPushRegister
import com.alibaba.sdk.android.push.register.OppoRegister
import com.alibaba.sdk.android.push.register.VivoRegister
import com.downloader.PRDownloader
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.sentry.android.core.SentryAndroid


class TomonApplication : Application(), Application.ActivityLifecycleCallbacks {

    private lateinit var sAnalytics: GoogleAnalytics
    private lateinit var sTracker: Tracker
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

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
        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationObserver())
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
                Logger.d("init cloudchannel success")
                initChannel()
            }

            override fun onFailed(errorCode: String, errorMessage: String) {
                Logger.d("init cloudchannel failed -- errorcode:$errorCode -- errorMessage:$errorMessage")
            }
        })

        MiPushRegister.register(applicationContext, "2882303761518514696", "5871851498696")
        HuaWeiRegister.register(this)
        OppoRegister.register(
            applicationContext,
            "09adbc3666374668b56fe6f36eded638",
            "b883bfa2d7a24978a4d9974487628305"
        )
        MeizuRegister.register(applicationContext, "132848", "e36250ebb8fe48e3b4c52636a19c28cb")
        VivoRegister.register(applicationContext)
    }

    @Synchronized
    fun getDefaultTracker(): Tracker {
        return sTracker
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
        }
    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityResumed(activity: Activity) {

    }
}