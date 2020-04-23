package cn.troph.tomon

import android.app.Application
import cn.troph.tomon.core.Client

class TomonApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Client.global.initialize(this)
    }
}