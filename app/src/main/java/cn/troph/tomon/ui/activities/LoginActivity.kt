package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.Validator
import cn.troph.tomon.ui.chat.viewmodel.DataPullingViewModel
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.HttpException
import java.util.concurrent.TimeUnit


data class LoginForm(
    val unionIdError: Int? = null,
    val passwordError: Int? = null
)

data class LoginResult(
    val success: Boolean,
    val error: Int? = null
)

class LoginViewModel : ViewModel() {

    private val _loginForm: MutableLiveData<LoginForm> by lazy {
        MutableLiveData<LoginForm>()
    }
    val loginForm: LiveData<LoginForm> = _loginForm

    private val _loginResult: MutableLiveData<LoginResult> by lazy {
        MutableLiveData<LoginResult>()
    }
    val loginResult: LiveData<LoginResult> = _loginResult

    fun loginDataValidate(union: String, password: String): Boolean {
        return when {
            !isUnionIdValid(union) -> {
                _loginForm.value = LoginForm(unionIdError = R.string.login_invalid_union_id)
                false
            }
            !isPasswordValid(password) -> {
                _loginForm.value = LoginForm(passwordError = R.string.login_invalid_password)
                false
            }
            else -> {
                _loginForm.value = LoginForm()
                true
            }
        }
    }

    private fun isUnionIdValid(id: String): Boolean {
        return when {
            id.contains("#") -> Validator.isFullName(id)
            id.contains("@") -> Validator.isEmail(id)
            else -> Validator.isPhone(id)
        }
    }

    private fun isPasswordValid(id: String): Boolean {
        return id.isNotEmpty()
    }

}

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        bindProgressButton(button_login)
        button_login.attachTextChangeAnimator()
        val dataPullingViewModel: DataPullingViewModel by viewModels()
        dataPullingViewModel.setUpFetchData()
        dataPullingViewModel.dataFetchLD.observe(this, Observer {
            if (it == true) {
                gotoChannelList()
            }
        })

        viewModel.loginForm.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer
            if (loginState.unionIdError != null) {
                register_input_user_name.error = getString(loginState.unionIdError)
            }
            if (loginState.passwordError != null) {
                register_input_union_id.error = getString(loginState.passwordError)
            }
        })

        button_login.setOnClickListener {
            button_login.isEnabled = false
            val uid = register_input_user_name.text.toString()
            val pw = register_input_union_id.text.toString()
            val valid = viewModel.loginDataValidate(uid, pw)
            if (valid) {
                button_login.showProgress {
                    progressColor = getColor(R.color.white)
                }
                Client.global.login(
                    unionId = uid,
                    password = pw
                ).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    button_login.hideProgress(R.string.login_succeed)
                    Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            //gotoChat()
                        }
                }, {
                    if (it is HttpException) {
                        button_login.hideProgress(if (it.code() >= 500) R.string.auth_server_error else R.string.login_failed)
                    }
                    Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            button_login.hideProgress(R.string.login_button)
                        }
                    button_login.isEnabled = true
                })
            } else
                button_login.isEnabled = true

        }
        layout_root.setOnClickListener {
            closeKeyboard()
        }
    }

    private fun closeKeyboard() {
        val view = currentFocus
        if (view != null) {
            view.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun gotoChannelList() {
        val intent = Intent(this, TomonMainActivity::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        startActivity(
            intent,
            ActivityOptions.makeCustomAnimation(
                this,
                R.animator.bottom_up_anim,
                R.animator.bottom_up_anim
            ).toBundle()
        )
    }

}
