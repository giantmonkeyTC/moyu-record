package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.AuthService
import cn.troph.tomon.core.utils.Validator
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import cn.troph.tomon.ui.widgets.TomonToast
import com.google.android.material.snackbar.Snackbar
import com.gyf.immersionbar.ImmersionBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_activity_register.*


class RegisterActivity : AppCompatActivity() {

    private val timer = object : CountDownTimer(30000, 1000) {
        override fun onFinish() {
            button_confirmation.text = getString(R.string.register_confirmation)
            button_confirmation.isEnabled = true
        }

        override fun onTick(millisUntilFinished: Long) {
            button_confirmation.text = "${millisUntilFinished / 1000} s"
        }
    }

    companion object {
        val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_activity_register)
        ImmersionBar.with(this).reset().init()
        ImmersionBar.with(this)
            .statusBarColor(R.color.blackPrimary, 0.2f)
            .navigationBarColor(R.color.blackPrimary)
            .fitsSystemWindows(true)
            .init()
        button_confirmation.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(button_confirmation.windowToken, 0)
            if (Validator.isPhone(register_input_union_id.text.toString())) {
                timer.start()
                it.isEnabled = false
                Client.global.rest.authService.verify(
                    AuthService.VerifyRequest(
                        phone = register_input_union_id.text.toString(),
                        type = "register"
                    )
                ).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe {
                }
            } else
                GeneralSnackbar.make(
                    GeneralSnackbar.findSuitableParent(button_confirmation)!!,
                    "请输入正确的手机号",
                    Snackbar.LENGTH_LONG
                ).show()
        }
        button_register.setOnClickListener {
            val username = register_input_user_name.text.toString().trim()
            val code = register_confirmation_code.text.toString().trim()
            val invite = register_input_invite_code.text.toString().trim()
            val unionId = register_input_union_id.text.toString().trim()
            val passwd = register_input_password.text.toString().trim()
            if (!Validator.isUserName(username))
                GeneralSnackbar.make(
                    GeneralSnackbar.findSuitableParent(button_confirmation)!!,
                    "用户名限制",
                    Snackbar.LENGTH_LONG
                ).show()
            else if (passwd.length < 8 || passwd.length > 32) {
                GeneralSnackbar.make(
                    GeneralSnackbar.findSuitableParent(button_confirmation)!!,
                    "密码长度需在 8 至 32 位之间",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            else
                Client.global.register(
                    username = username,
                    code = code,
                    invite = invite,
                    password = passwd,
                    unionId = unionId
                ).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    TomonToast.makeText(applicationContext, getString(R.string.regist_successed), Toast.LENGTH_LONG).show()
                    gotoEntryOption()
                }, {
                    it.message?.let {
                        Log.e(TAG, "error message: " + it)
                        if (it.contains("422")) {
                            GeneralSnackbar.make(
                                GeneralSnackbar.findSuitableParent(button_confirmation)!!,
                                getString(R.string.user_existed_info_error),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        } else {
                            GeneralSnackbar.make(
                                GeneralSnackbar.findSuitableParent(button_confirmation)!!,
                                getString(R.string.regist_unknown_error) + it,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                    }

                }, {

                })
        }

        val ss = SpannableString(getString(R.string.privacy_agreement))
        val ss2 = SpannableString(getString(R.string.privacy_agreement_pri))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val alert: AlertDialog.Builder = AlertDialog.Builder(this@RegisterActivity)
                alert.setTitle("TOMON")
                val wv = WebView(this@RegisterActivity)
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
                val alert: AlertDialog.Builder = AlertDialog.Builder(this@RegisterActivity)
                alert.setTitle("TOMON")
                val wv = WebView(this@RegisterActivity)
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


    private fun gotoEntryOption() {
        val intent = Intent(this, EntryOptionActivity::class.java)
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
