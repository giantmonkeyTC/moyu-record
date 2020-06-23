package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
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
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                register_n_login as View,
                "register_n_login"
            ).toBundle()
        )
    }

    private fun gotoRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(
            intent,
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                register_n_login as View,
                "register_n_login"
            ).toBundle()
        )
    }
}