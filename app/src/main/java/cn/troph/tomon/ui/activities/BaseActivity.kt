package cn.troph.tomon.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.troph.tomon.R
import cn.troph.tomon.TomonApplication
import com.google.android.gms.analytics.Tracker
import com.gyf.immersionbar.ImmersionBar

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarColor(R.color.blackPrimary, 0.0f)
            .keyboardEnable(true)
            .navigationBarColor(R.color.blackPrimary)
            .fitsSystemWindows(true)
            .init()
    }

    fun getTracker(): Tracker {
        return (application as TomonApplication).getDefaultTracker()
    }

}