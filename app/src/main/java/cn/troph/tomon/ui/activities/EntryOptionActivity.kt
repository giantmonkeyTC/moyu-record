package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import cn.troph.tomon.R
import cn.troph.tomon.core.utils.DensityUtil
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.layout_activity_entry_option.*

val LOGIN_NUMBER: Int = 0
val LOGIN_PASSWORD: Int = 1
class EntryOptionActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).reset().init()
        setContentView(R.layout.layout_activity_entry_option)
        button_login_route.setOnClickListener {
            loginWithPwd()
        }

        button_register_route.setOnClickListener {
            loginWithNumber() }

        setPrivacyLink()
    }

    private fun setPrivacyLink() {
        val ss = SpannableString(getString(R.string.privacy_agreement))
        val ss2 = SpannableString(getString(R.string.privacy_agreement_pri))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val alert: AlertDialog.Builder = AlertDialog.Builder(this@EntryOptionActivity)
                alert.setTitle("TOMON")
                val wv = WebView(this@EntryOptionActivity)
                wv.loadUrl("https://www.tomon.co/terms")
                wv.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        url: String
                    ): Boolean {
                        view.loadUrl(url)
                        return true
                    }
                }
                wv.settings.javaScriptEnabled = true
                alert.setView(wv)
                alert.setNegativeButton("关闭", { dialog, id -> dialog.dismiss() })
                alert.show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        val clickableSpan2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val alert: AlertDialog.Builder = AlertDialog.Builder(this@EntryOptionActivity)
                alert.setTitle("TOMON")
                val wv = WebView(this@EntryOptionActivity)
                wv.loadUrl("https://www.tomon.co/privacy")
                wv.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        url: String
                    ): Boolean {
                        view.loadUrl(url)
                        return true
                    }

                }
                wv.settings.javaScriptEnabled = true
                alert.setView(wv)
                alert.setNegativeButton("关闭", { dialog, id -> dialog.dismiss() })
                alert.show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        ss.setSpan(clickableSpan, 5, 16, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        ss2.setSpan(clickableSpan2, 0, 11, Spanned.SPAN_INCLUSIVE_INCLUSIVE)


        private_agreement_tv.text = ss
        private_agreement_tv.movementMethod = LinkMovementMethod.getInstance()
        private_agreement_tv.highlightColor = Color.TRANSPARENT

        private_agreement_tv2.text = ss2
        private_agreement_tv2.movementMethod = LinkMovementMethod.getInstance()
        private_agreement_tv2.highlightColor = Color.TRANSPARENT
    }

    override fun onResume() {
        super.onResume()
        val mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0]
                    val character = findViewById<ImageView>(R.id.login_character)
                    character.translationX = if (x < 0)
                        DensityUtil.dip2px(
                            applicationContext, ((x.unaryMinus() * 5).toFloat())
                        ).toFloat()
                    else
                        DensityUtil.dip2px(applicationContext, ((x * 5).toFloat()))
                            .toFloat()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }
        }, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST)

    }


    private fun loginWithNumber() {
        val intent = Intent(this, OptionalLoginActivity::class.java)
        intent.putExtra("login_type", LOGIN_NUMBER)
        startActivity(intent,ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_right_custom,
            R.anim.no_animation
        ).toBundle())
    }

    private fun loginWithPwd() {
        val intent = Intent(this, OptionalLoginActivity::class.java)
        intent.putExtra("login_type", LOGIN_PASSWORD)
        startActivity(intent,ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_right_custom,
            R.anim.no_animation
        ).toBundle())
    }
}