package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.Validator
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
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
        val view = findViewById<ConstraintLayout>(R.id.layout_root)
        val union = findViewById<EditText>(R.id.input_union_id)
        val password = findViewById<EditText>(R.id.input_password)
        val button = findViewById<Button>(R.id.button_login)
        bindProgressButton(button)
        button.attachTextChangeAnimator()

        viewModel.loginForm.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer
            if (loginState.unionIdError != null) {
                union.error = getString(loginState.unionIdError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        button.setOnClickListener {
            val uid = union.text.toString()
            val pw = password.text.toString()
            val valid = viewModel.loginDataValidate(uid, pw)
            if (valid) {
                button.showProgress {
                    progressColor = getColor(R.color.white)
                }
                Client.global.login(
                    unionId = uid,
                    password = pw
                ).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    button.hideProgress(R.string.login_succeed)
                    Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            gotoChat()
                        }
                }, {
                    val e = it as HttpException
                    button.hideProgress(if (e.code() >= 500) R.string.auth_server_error else R.string.login_failed)
                    Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            button.hideProgress(R.string.login_button)
                        }
                })
            }

        }
        view.setOnClickListener {
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

    private fun gotoChat() {
        val intent = Intent(this, ChatActivity::class.java)
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
