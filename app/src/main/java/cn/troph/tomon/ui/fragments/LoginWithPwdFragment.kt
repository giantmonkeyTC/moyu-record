package cn.troph.tomon.ui.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.AuthService
import cn.troph.tomon.core.utils.Validator
import cn.troph.tomon.ui.activities.OptionalLoginActivity
import cn.troph.tomon.ui.activities.TomonMainActivity
import cn.troph.tomon.ui.chat.viewmodel.DataPullingViewModel
import cn.troph.tomon.ui.widgets.TomonToast
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_login_verification.*
import kotlinx.android.synthetic.main.layout_login_verification.view.*
import kotlinx.android.synthetic.main.layout_login_with_pwd.*
import kotlinx.android.synthetic.main.layout_login_with_pwd.private_agreement_tv
import kotlinx.android.synthetic.main.layout_login_with_pwd.private_agreement_tv2
import kotlinx.android.synthetic.main.layout_login_with_pwd.view.*
import kotlinx.android.synthetic.main.layout_login_with_pwd.view.textView17
import retrofit2.HttpException
import java.util.concurrent.TimeUnit


class LoginWithPwdFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_login_with_pwd, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindProgressButton(login_with_pwd.button5)
        login_with_pwd.button5.attachTextChangeAnimator()
        val dataPullingViewModel: DataPullingViewModel by activityViewModels()
        dataPullingViewModel.setUpFetchData()
        dataPullingViewModel.dataFetchLD.observe(requireActivity(), Observer {
            if (it == true) {
                gotoChannelList()
            }
        })
        setPrivacyLink()
        textView18.setOnClickListener {
            val loginNumberFragment = LoginWithNumberFragment()
            loginNumberFragment.arguments = Bundle().apply {
                putInt(PHONE_CODE_TYPE, PHONE_CODE_TYPE_LOGIN)
            }
            fragmentReplace(loginNumberFragment)
        }
        login_with_pwd.textView17.setOnClickListener {
            val loginWithNumberFragment = LoginWithNumberFragment()
            loginWithNumberFragment.arguments = Bundle().apply {
                putInt(PHONE_CODE_TYPE, PHONE_CODE_TYPE_FORGET)
            }
            fragmentAdd(loginWithNumberFragment)
        }
        login_with_pwd.pwd_register.setOnClickListener {
            if (validation()) {
                val phone = login_with_pwd.editText2.text()
                val pwd = login_with_pwd.editText4.text.toString()
                Client.global.rest.authService.verify(
                    AuthService.VerifyRequest(
                        phone = phone,
                        type = TYPE_REGISTER
                    )
                ).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe {
                    login_with_pwd.pwd_register.isEnabled = true
                    val verifyCodeFragment = VerifyCodeFragment()
                    verifyCodeFragment.arguments = Bundle().apply {
                        putInt(REGISTER_TYPE, REGISTER_TYPE_WITH_PWD)
                        putInt(VERIFY_TYPE, VERIFY_TYPE_REGISTER)
                        getUserInfo().phone = phone
                        getUserInfo().pwd = pwd
                        putString(VERIFY_PHONE, phone)
                    }
                    fragmentAdd(verifyCodeFragment)
                }
            }
        }
        login_with_pwd.button5.setOnClickListener {
            if (validation())
                login()
        }

    }

    private fun validation(): Boolean {
        val phone = login_with_pwd.editText2.text()
        val pwd = login_with_pwd.editText4.text.toString()
        if (!Validator.isPhone(phone)) {
            TomonToast.makeErrorText(
                requireContext(),
                getString(R.string.wrong_phone),
                Toast.LENGTH_LONG
            ).show()
            return false
        } else if (pwd.length < 8 || pwd.length > 32) {
            TomonToast.makeErrorText(
                requireContext(),
                getString(R.string.login_new_pwd_hint),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun getUserInfo(): OptionalLoginActivity.UserInfo {
        return (requireActivity() as OptionalLoginActivity).userInfo
    }

    private fun fragmentReplace(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.content, fragment)
        }.commit()
    }

    private fun getInstance(): LoginWithPwdFragment {
        return this
    }

    private fun fragmentAdd(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in_right_custom,
                R.anim.no_animation
            )
            add(R.id.content, fragment)
            hide(getInstance())
            addToBackStack(null)
        }.commit()
    }

    private fun setPrivacyLink() {
        val ss = SpannableString(getString(R.string.privacy_agreement))
        val ss2 = SpannableString(getString(R.string.privacy_agreement_pri))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val alert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                alert.setTitle("TOMON")
                val wv = WebView(requireContext())
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
                val alert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                alert.setTitle("TOMON")
                val wv = WebView(requireContext())
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

    private fun login() {
        Client.global.login(
            unionId = login_with_pwd.editText2.text(),
            password = login_with_pwd.editText4.text.toString()
        ).observeOn(AndroidSchedulers.mainThread()).subscribe({
            login_with_pwd.button5.hideProgress(R.string.login_succeed)
            Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                }
        }, {
            if (it is HttpException) {
                login_with_pwd.button5.hideProgress(if (it.code() >= 500) R.string.auth_server_error else R.string.login_failed)
            }
            Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    login_with_pwd.button5.hideProgress(R.string.login_button)
                }
            login_with_pwd.button5.isEnabled = true
        })
    }

    private fun gotoChannelList() {
        val intent = Intent(requireContext(), TomonMainActivity::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        startActivity(
            intent,
            ActivityOptions.makeCustomAnimation(
                requireContext(),
                R.animator.bottom_up_anim,
                R.animator.bottom_up_anim
            ).toBundle()
        )
    }
}