package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import cn.troph.tomon.R
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_entry_option.*
import kotlinx.android.synthetic.main.chat_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_channel_panel.*

class EntryOptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_entry_option)
        button_login_route.setOnClickListener {
            gotoLogin()
        }
        button_register_route.setOnClickListener { gotoRegister() }
    }


    private fun gotoLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(
            intent,
            ActivityOptions.makeCustomAnimation(
                this,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            ).toBundle()
        )
        finish()
    }

    private fun gotoRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(
            intent,
            ActivityOptions.makeCustomAnimation(
                this,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            ).toBundle()
        )
        finish()
    }
}