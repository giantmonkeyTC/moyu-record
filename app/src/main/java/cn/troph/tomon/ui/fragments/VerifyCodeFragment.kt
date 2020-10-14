package cn.troph.tomon.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import cn.troph.tomon.R

import kotlinx.android.synthetic.main.layout_login_verification.*
import kotlinx.android.synthetic.main.layout_login_verification.view.*
import java.lang.NullPointerException

val VERIFY_TYPE_LOGIN = 0
val VERIFY_TYPE_FORGET_PWD = 1
val VERIFY_TYPE = "verify_type"
val VERIFICATION_CODE_MAX_LENGTH = 4

class VerifyCodeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return arguments?.let {
            if (it.getInt(VERIFY_TYPE) == VERIFY_TYPE_LOGIN)
                return@let inflater.inflate(R.layout.layout_login_verification, null)
            else if (it.getInt(VERIFY_TYPE) == VERIFY_TYPE_FORGET_PWD)
                return@let inflater.inflate(R.layout.layout_forget_pwd_verification, null)
            else
                throw NullPointerException("no argument passed")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            when (it.getInt(VERIFY_TYPE)) {
                VERIFY_TYPE_LOGIN -> LoginConfig()
                VERIFY_TYPE_FORGET_PWD -> forgetPwdConfig()
            }
        }
    }

    private fun LoginConfig() {
        login_verification.verification_et.doOnTextChanged { text, start, before, count ->
            val textLength = login_verification.verification_et.length()
            if (textLength == VERIFICATION_CODE_MAX_LENGTH) {
                login_verification.next.visibility = View.VISIBLE
            }
            else
                login_verification.next.visibility = View.GONE
        }
        val timer = object : CountDownTimer(60000, 1000) {
            override fun onFinish() {
                login_verification.resend_code.text = getString(R.string.resend)
                login_verification.resend_code.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                login_verification.resend_code.text = "${millisUntilFinished / 1000} s"
            }
        }
        timer.start()

        timer.cancel()
    }



    private fun forgetPwdConfig() {

    }


}