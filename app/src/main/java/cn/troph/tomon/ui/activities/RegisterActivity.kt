package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.AuthService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        button_confirmation.setOnClickListener {
            Client.global.rest.authService.verify(
                AuthService.VerifyRequest(
                    phone = register_input_union_id.text.toString(),
                    type = "register"
                )
            ).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe {

            }
        }
        button_register.setOnClickListener {
            val username = register_input_user_name.text.toString()
            val code = register_confirmation_code.text.toString()
            val invite = register_input_invite_code.text.toString()
            val unionId = register_input_union_id.text.toString()

            Client.global.register(
                username = username,
                code = code,
                invite = invite,
                unionId = unionId
            ).observeOn(AndroidSchedulers.mainThread()).subscribe {

            }
        }
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
}
