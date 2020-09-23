package cn.troph.tomon.ui.activities

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.Client.Companion.global
import cn.troph.tomon.core.network.services.AuthService
import cn.troph.tomon.core.utils.KeyboardUtils
import cn.troph.tomon.ui.widgets.TomonToast
import com.gyf.immersionbar.ImmersionBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_me_settings.*
import retrofit2.HttpException

class MeSettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).reset().init()
        ImmersionBar.with(this)
            .statusBarColor(R.color.blackPrimary)
            .keyboardEnable(true)
            .keyboardMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            .navigationBarColor(R.color.blackPrimary)
            .fitsSystemWindows(true)
            .init()
        setContentView(R.layout.activity_me_settings)
        val bundle = intent.extras
        val property = bundle?.get("property") as String
        btn_change_confirm.setEnabled(false)
        when (property) {
            "name" -> {
                me_settings_user_avatar.user = Client.global.me
                me_settings_content.setText(Client.global.me.name)
                me_profile_properties.text = getString(R.string.change_name)
                group.visibility = View.GONE
            }
            "username" -> {
                me_settings_user_avatar.user = Client.global.me
                me_settings_content.setText(Client.global.me.username)
                me_profile_properties.text = getString(R.string.change_username)
            }
            "email" -> {
                me_settings_user_avatar.user = Client.global.me
                me_settings_content.setText(if (Client.global.me.email == null) "" else Client.global.me.email)
                me_profile_properties.text = getString(R.string.change_email)
            }
            "phone" -> {
                me_settings_user_avatar.user = Client.global.me
                me_settings_content.setText(Client.global.me.phone)
                me_profile_properties.text = getString(R.string.change_phone)
            }
        }
        me_pwd_iv_clear.setOnClickListener {
            me_pwd_content.setText("")
        }
        me_settings_iv_clear.setOnClickListener {
            me_settings_content.setText("")
        }
        btn_change_confirm.setOnClickListener {
            confirmChange(property)
        }
        iv_back.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right_custom)
        }
        me_settings_content.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 0)
                    me_settings_iv_clear.visibility = View.GONE
                else
                    me_settings_iv_clear.visibility = View.VISIBLE
                updateFinishButtonStyle(property)
            }

        })
        me_pwd_content.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 0)
                    me_pwd_iv_clear.visibility = View.GONE
                else
                    me_pwd_iv_clear.visibility = View.VISIBLE
                updateFinishButtonStyle(property)
            }

        })
        me_pwd_content.setText("")
    }

    private fun updateFinishButtonStyle(property: String) {
        when (property) {
            "name" -> {
                if (me_settings_content.text.length > 16 || me_settings_content.text.isEmpty() || me_settings_content.text.toString() == global.me.name) {
                    btn_change_confirm.setEnabled(false)
                    btn_change_confirm.setTextColor(getColor(R.color.white_70))
                } else {
                    btn_change_confirm.setEnabled(true)
                    btn_change_confirm.setTextColor(getColor(R.color.white))
                }
            }
            "username" -> {
                if (me_settings_content.text.length > 16 || me_settings_content.text.isEmpty() || me_settings_content.text.toString() == global.me.username || me_pwd_content.text.isEmpty()) {
                    btn_change_confirm.setEnabled(false)
                    btn_change_confirm.setTextColor(getColor(R.color.white_70))
                } else {
                    btn_change_confirm.setEnabled(true)
                    btn_change_confirm.setTextColor(getColor(R.color.white))
                }
            }
            "email" -> {
                if (me_settings_content.text.isEmpty() || me_settings_content.text.toString() == global.me.email || me_pwd_content.text.isEmpty()) {
                    btn_change_confirm.setEnabled(false)
                    btn_change_confirm.setTextColor(getColor(R.color.white_70))
                } else {
                    btn_change_confirm.setEnabled(true)
                    btn_change_confirm.setTextColor(getColor(R.color.white))
                }
            }
            "phone" -> {
                btn_change_confirm.setEnabled(false)
            }
            else -> {
                btn_change_confirm.setEnabled(false)
            }
        }

    }

    private fun confirmChange(property: String) {
        val content: String = me_settings_content.getText().toString().trim({ it <= ' ' })
        val pwd: String = me_pwd_content.getText().toString().trim({ it <= ' ' })
        val request: AuthService.MeSettingsRequest = when (property) {
            "name" -> {
                AuthService.MeSettingsRequest(name = content)
            }
            "username" -> {
                AuthService.MeSettingsRequest(username = content, password = pwd)
            }
            "email" -> {
                AuthService.MeSettingsRequest(email = content, password = pwd)
            }
            "phone" -> {
                AuthService.MeSettingsRequest()
            }
            else -> {
                AuthService.MeSettingsRequest()
            }
        }
        global.rest.authService.meSettings(
            global.auth,
            request
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                TomonToast.makeText(
                    applicationContext,
                    getString(R.string.set_nickname_success),
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)
                finish()
                overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right_custom)
            }) {
                if (it is HttpException) {
                    if (it.code() == 401)
                        TomonToast.makeErrorText(
                            applicationContext,
                            getString(R.string.wrong_password),
                            Toast.LENGTH_SHORT
                        ).show()
                } else
                    TomonToast.makeErrorText(
                        applicationContext,
                        getString(R.string.me_settings_failed),
                        Toast.LENGTH_SHORT
                    ).show()
            }
    }

    override fun onResume() {
        super.onResume()
        KeyboardUtils.showKeyBoard(me_settings_content, this)
    }

    override fun onPause() {
        super.onPause()
        KeyboardUtils.hideKeyBoard(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right_custom)
    }

}