package cn.troph.tomon.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.AuthService
import cn.troph.tomon.ui.widgets.TomonToast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.login_forget_pwd_set_pwd.*

class ResetPwdFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_forget_pwd_set_pwd, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        confirm_reset_pwd.setOnClickListener {
            if (validation(new_pwd.text.toString(), new_pwd_repeat.text.toString()))
                Client.global.rest.authService.newPwd(
                    request = AuthService.NewPwdRequest(
                        newPwd = new_pwd.text.toString(),
                        reset = arguments?.getString(RESET_PWD_TOKEN)
                    )
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    fragmentReplace(LoginWithPwdFragment())
                }
        }
    }

    private fun validation(pwd: String, pwdRepeat: String): Boolean {
        if (pwd.length < 8 || pwd.length > 32) {
            TomonToast.makeErrorText(
                requireContext(),
                getString(R.string.login_new_pwd_hint),
                Toast.LENGTH_LONG
            ).show()
            return false
        } else if (pwdRepeat != pwd) {
            TomonToast.makeErrorText(
                requireContext(),
                getString(R.string.different_pwd_hint),
                Toast.LENGTH_LONG
            ).show()
            return false
        } else
            return true
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
}