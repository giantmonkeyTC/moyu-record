package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.troph.tomon.R
import kotlinx.android.synthetic.main.activity_entry_option.*

class EntryOptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }
}