package cn.troph.tomon.ui.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.AuthService
import cn.troph.tomon.ui.activities.OptionalLoginActivity
import cn.troph.tomon.ui.activities.TomonMainActivity
import cn.troph.tomon.ui.chat.viewmodel.DataPullingViewModel
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_login_verification.*
import kotlinx.android.synthetic.main.layout_login_verification.view.*
import retrofit2.HttpException
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit

val VERIFY_TYPE_LOGIN = 0
val VERIFY_TYPE_FORGET_PWD = 1
val VERIFY_TYPE_REGISTER = 2
val VERIFY_TYPE = "verify_type"
val VERIFICATION_CODE_MAX_LENGTH = 4
val VERIFY_PHONE = "verify_phone"
val TYPE_LOGIN = "login"
val TYPE_REGISTER = "register"
val REGISTER_TYPE = "register_type"
val REGISTER_TYPE_WITH_PWD = 0
val REGISTER_TYPE_WITH_NUMBER = 1


class VerifyCodeFragment : Fragment() {
    private lateinit var timer: CountDownTimer
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
            else if (it.getInt(VERIFY_TYPE) == VERIFY_TYPE_REGISTER)
                return@let inflater.inflate(R.layout.layout_login_verification, null)
            else
                throw NullPointerException("no argument passed")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            when (it.getInt(VERIFY_TYPE)) {
                VERIFY_TYPE_LOGIN -> loginConfig()
                VERIFY_TYPE_FORGET_PWD -> forgetPwdConfig()
                VERIFY_TYPE_REGISTER -> registerConfig()
            }
        }
    }

    private fun registerConfig() {
        login_verification.verification_title.text = getString(R.string.register_verification)
        timer = object : CountDownTimer(60000, 1000) {
            override fun onFinish() {
                login_verification.resend_code.background.setTint(requireContext().getColor(R.color.primaryColor))
                login_verification.resend_code.text = getString(R.string.resend)
                login_verification.resend_code.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                login_verification.resend_code.text = "冷却中：${millisUntilFinished / 1000}秒"
            }
        }
        login_verification.resend_code.isEnabled = false
        login_verification.resend_code.background.setTint(requireContext().getColor(R.color.whiteAlpha20))
        sendCodeRequest(type = TYPE_REGISTER)
        login_verification.resend_code.setOnClickListener {
            login_verification.resend_code.isEnabled = false
            login_verification.resend_code.background.setTint(requireContext().getColor(R.color.whiteAlpha20))
            sendCodeRequest(type = TYPE_REGISTER)
        }
        login_verification.next.setOnClickListener {
            login_verification.verification_et.text?.let { edit ->
                if (edit.length == VERIFICATION_CODE_MAX_LENGTH)
                    arguments?.getInt(REGISTER_TYPE)?.let {
                        if (it == REGISTER_TYPE_WITH_PWD) {
                            getUserInfo().verification = edit.toString()
                            activateAccount()
                        } else if (it == REGISTER_TYPE_WITH_NUMBER) {
                            getUserInfo().verification = edit.toString()
                            setPwd()
                        }
                    }
            }
        }
        login_verification.verification_et.doOnTextChanged { text, start, before, count ->
            val textLength = login_verification.verification_et.length()
            if (textLength == VERIFICATION_CODE_MAX_LENGTH) {
                login_verification.next.visibility = View.VISIBLE
                arguments?.getInt(REGISTER_TYPE)?.let {
                    if (it == REGISTER_TYPE_WITH_PWD)
                        activateAccount()
                    else if (it == REGISTER_TYPE_WITH_NUMBER)
                        setPwd()
                }
            } else
                login_verification.next.visibility = View.GONE
        }
    }

    private fun getUserInfo(): OptionalLoginActivity.UserInfo {
        return (requireActivity() as OptionalLoginActivity).userInfo
    }

    private fun activateAccount() {
        val fragment = ActivateAccountFragment()
        fragmentAdd(fragment)
    }

    private fun setPwd() {
        val fragment = SetPwdFragment()
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

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
    }

    private fun loginConfig() {
        bindProgressButton(login_verification.next)
        login_verification.next.attachTextChangeAnimator()
        val dataPullingViewModel: DataPullingViewModel by activityViewModels()
        dataPullingViewModel.setUpFetchData()
        dataPullingViewModel.dataFetchLD.observe(requireActivity(), Observer {
            if (it == true) {
                gotoChannelList()
            }
        })
        timer = object : CountDownTimer(60000, 1000) {
            override fun onFinish() {
                login_verification.resend_code.background.setTint(requireContext().getColor(R.color.primaryColor))
                login_verification.resend_code.text = getString(R.string.resend)
                login_verification.resend_code.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                login_verification.resend_code.text = "冷却中：${millisUntilFinished / 1000}秒"
            }
        }
        login_verification.resend_code.isEnabled = false
        login_verification.resend_code.background.setTint(requireContext().getColor(R.color.whiteAlpha20))
        login_verification.resend_code.setOnClickListener {
            login_verification.resend_code.isEnabled = false
            login_verification.resend_code.background.setTint(requireContext().getColor(R.color.whiteAlpha20))
            sendCodeRequest(type = TYPE_LOGIN)
        }
        login_verification.next.setOnClickListener {
            login_verification.verification_et.text?.let {
                if (it.length == VERIFICATION_CODE_MAX_LENGTH)
                    login()
            }
        }
        login_verification.verification_et.doOnTextChanged { text, start, before, count ->
            val textLength = login_verification.verification_et.length()
            if (textLength == VERIFICATION_CODE_MAX_LENGTH) {
                login_verification.next.visibility = View.VISIBLE
                login()
            } else
                login_verification.next.visibility = View.GONE
        }
    }

    private fun login() {
        Client.global.login(
            unionId = arguments?.getString(VERIFY_PHONE),
            code = login_verification.verification_et.text.toString()
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

    private fun register() {

    }

    private fun sendCodeRequest(type: String) {
        arguments?.getString(VERIFY_PHONE)?.let {
            timer.start()
            Client.global.rest.authService.verify(
                AuthService.VerifyRequest(
                    phone = it,
                    type = type
                )
            ).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe {
            }
        }
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


    private fun forgetPwdConfig() {

    }


}