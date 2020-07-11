package cn.troph.tomon.ui.activities

import androidx.appcompat.app.AppCompatActivity
import cn.troph.tomon.TomonApplication
import com.google.android.gms.analytics.Tracker

open class BaseActivity : AppCompatActivity() {

    fun getTracker(): Tracker {
        return (application as TomonApplication).getDefaultTracker()
    }

}