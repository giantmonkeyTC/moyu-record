package cn.troph.tomon.ui.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cn.troph.tomon.R
import cn.troph.tomon.core.utils.Validator
import kotlinx.android.synthetic.main.layout_activity_login.*
import kotlinx.android.synthetic.main.layout_login_with_number.*
import kotlinx.android.synthetic.main.layout_login_with_number.private_agreement_tv
import kotlinx.android.synthetic.main.layout_login_with_number.private_agreement_tv2
import kotlinx.android.synthetic.main.layout_login_with_number.view.*

data class LoginForm(
    val phoneError: Int? = null
)

class LoginViewModel : ViewModel() {
    private val _loginForm: MutableLiveData<LoginForm> = MutableLiveData()
    val loginForm: LiveData<LoginForm> = _loginForm
    fun loginDataValidate(union: String): Boolean {
        return when {
            !isPhoneValid(union) -> {
                _loginForm.value = LoginForm(phoneError = R.string.wrong_phone)
                false
            }
            else -> {
                _loginForm.value = LoginForm()
                true
            }
        }
    }

    private fun isPhoneValid(id: String): Boolean {
        return Validator.isPhone(id)
    }

}

class LoginWithNumberFragment : Fragment() {
    private val viewModel: LoginViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_login_with_number, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setPrivacyLink()
        login_with_number.editText2.inputType = InputType.TYPE_CLASS_PHONE
        login_with_number.editText2.setRawInputType(Configuration.KEYBOARD_QWERTY)
        viewModel.loginForm.observe(requireActivity(), Observer {
            val loginState = it ?: return@Observer
            if (loginState.phoneError != null) {
                login_with_number.editText2.error = getString(loginState.phoneError)
            }
        })
        login_with_number.textView17.setOnClickListener {
            val loginPwdFragment = LoginWithPwdFragment()
            fragmentReplace(loginPwdFragment)
        }
        login_with_number.next.setOnClickListener {
            login_with_number.next.isEnabled = false
            val phone = login_with_number.editText2.text.toString()
            val valid = viewModel.loginDataValidate(phone)
            if (valid) {
                login_with_number.next.isEnabled = true
                val verifyCodeFragment = VerifyCodeFragment()
                verifyCodeFragment.arguments = Bundle().apply {
                    putInt(VERIFY_TYPE, VERIFY_TYPE_LOGIN)
                    putString(VERIFY_PHONE,phone)
                }
                fragmentAdd(verifyCodeFragment)
            } else
                login_with_number.next.isEnabled = true
        }
    }

    private fun getInstance(): LoginWithNumberFragment {
        return this
    }

    private fun fragmentReplace(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in_right_custom,
                R.anim.no_animation
            )
            replace(R.id.content, fragment)
        }.commit()
    }

    private fun fragmentAdd(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in_right_custom,
                R.anim.no_animation
            )
            add(R.id.content, fragment)
//            hide(getInstance())
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
}