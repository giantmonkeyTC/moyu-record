package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.inputmethodservice.Keyboard
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.AuthService
import cn.troph.tomon.core.utils.Validator
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        button_confirmation.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(button_confirmation.windowToken, 0)
            if (Validator.isPhone(register_input_union_id.text.toString()))
                Client.global.rest.authService.verify(
                    AuthService.VerifyRequest(
                        phone = register_input_union_id.text.toString(),
                        type = "register"
                    )
                ).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe {

                }
            else
                GeneralSnackbar.make(
                    GeneralSnackbar.findSuitableParent(button_confirmation)!!,
                    "请输入正确的手机号",
                    Snackbar.LENGTH_LONG
                ).show()
        }
        button_register.setOnClickListener {


            val username = register_input_user_name.text.toString()
            val code = register_confirmation_code.text.toString()
            val invite = register_input_invite_code.text.toString()
            val unionId = register_input_union_id.text.toString()
            if (!Validator.isUserName(username))
                GeneralSnackbar.make(
                    GeneralSnackbar.findSuitableParent(button_confirmation)!!,
                    "用户名限制",
                    Snackbar.LENGTH_LONG
                ).show()
            else
                Client.global.register(
                    username = username,
                    code = code,
                    invite = invite,
                    unionId = unionId
                ).observeOn(AndroidSchedulers.mainThread()).subscribe({

                }, {
                    GeneralSnackbar.make(
                        GeneralSnackbar.findSuitableParent(button_confirmation)!!,
                        "注册失败",
                        Snackbar.LENGTH_LONG
                    ).show()
                }, {})
        }
    }
}
