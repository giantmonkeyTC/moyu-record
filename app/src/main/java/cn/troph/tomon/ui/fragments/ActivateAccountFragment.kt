package cn.troph.tomon.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlinx.android.synthetic.main.layout_activity_register.*
import kotlinx.android.synthetic.main.layout_enable_account.*
import kotlinx.android.synthetic.main.layout_enable_account.view.*
import kotlinx.android.synthetic.main.layout_login_verification.*
import kotlinx.android.synthetic.main.layout_login_verification.view.*
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
        activate_account.enter_tomon.setOnClickListener {
            val activateCode = activate_account.editText2.text.toString()
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
}