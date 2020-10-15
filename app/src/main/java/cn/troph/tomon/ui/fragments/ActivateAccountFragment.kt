package cn.troph.tomon.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.activities.OptionalLoginActivity
import cn.troph.tomon.ui.activities.RegisterActivity
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import cn.troph.tomon.ui.widgets.TomonToast
import com.github.razir.progressbutton.hideProgress
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.fragment_reaction.*
import kotlinx.android.synthetic.main.layout_activity_register.*
import kotlinx.android.synthetic.main.layout_activity_register.private_agreement_tv
import kotlinx.android.synthetic.main.layout_activity_register.private_agreement_tv2
import kotlinx.android.synthetic.main.layout_enable_account.*
import kotlinx.android.synthetic.main.layout_enable_account.view.*
import kotlinx.android.synthetic.main.layout_login_verification.*
import kotlinx.android.synthetic.main.layout_login_verification.view.*
import kotlinx.android.synthetic.main.layout_login_with_number.*
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class ActivateAccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_enable_account, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setApplyLink()
        activate_account.enter_tomon.setOnClickListener {
            getUserInfo().activateCode = activate_account.editText2.text.toString()
            completeInfo()
            Client.global.register(
                code = getUserInfo().verification,
                invite = getUserInfo().activateCode,
                password = getUserInfo().pwd,
                unionId = getUserInfo().phone
            ).observeOn(AndroidSchedulers.mainThread()).subscribe({
                TomonToast.makeText(
                    requireContext(),
                    getString(R.string.regist_successed),
                    Toast.LENGTH_LONG
                ).show()
                login(getUserInfo().phone!!, getUserInfo().pwd!!)
            }, {
                it.message?.let {
                    Log.e(RegisterActivity.TAG, "error message: " + it)
                    if (it.contains("422")) {
                        TomonToast.makeErrorText(
                            requireContext(),
                            getString(R.string.user_existed_info_error),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        TomonToast.makeErrorText(
                            requireContext(),
                            getString(R.string.regist_unknown_error) + it,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

            }, {

            })
        }
    }

    private fun getUserInfo(): OptionalLoginActivity.UserInfo {
        return (requireActivity() as OptionalLoginActivity).userInfo
    }

    private fun login(phone: String, pwd: String) {
        Client.global.login(
            unionId = phone,
            password = pwd
        ).observeOn(AndroidSchedulers.mainThread()).subscribe({
            login_verification.next.hideProgress(R.string.login_succeed)
            Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                }
        }, {
            if (it is HttpException) {
                login_verification.next.hideProgress(if (it.code() >= 500) R.string.auth_server_error else R.string.login_failed)
            }
            Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    login_verification.next.hideProgress(R.string.login_button)
                }
            login_verification.next.isEnabled = true
        })
    }

    private fun setApplyLink() {
        val ss = SpannableString(getString(R.string.apply_code))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                applyInviteCode()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        ss.setSpan(clickableSpan, 8, 10, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        ss.setSpan(
            ForegroundColorSpan(requireActivity().getColor(R.color.primaryColor)),
            8,
            10,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        activate_account.textView15.text = ss
        activate_account.textView15.movementMethod = LinkMovementMethod.getInstance()
        activate_account.textView15.highlightColor = Color.TRANSPARENT
    }

    private fun applyInviteCode() {
        val fragment = ApplyInviteCodeFragment()
        fragmentAdd(fragment)
    }

    private fun completeInfo() {
        val fragment = CompleteInfoFragment()
        fragmentAdd(fragment)
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

    private fun getInstance(): Fragment {
        return this
    }
}