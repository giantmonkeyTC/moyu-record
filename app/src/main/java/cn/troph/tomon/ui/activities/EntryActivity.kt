package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)
        Client.global
            .login()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                gotoChat()
            }, {
                gotoLogin()
            })
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

    private fun gotoChat() {
        val intent = Intent(this, ChatActivity::class.java)
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
