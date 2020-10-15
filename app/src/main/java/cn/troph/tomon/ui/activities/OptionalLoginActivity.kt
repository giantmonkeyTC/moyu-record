package cn.troph.tomon.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cn.troph.tomon.R
import cn.troph.tomon.ui.chat.fragments.GuildChannelSelectorFragment
import cn.troph.tomon.ui.fragments.LoginWithNumberFragment
import cn.troph.tomon.ui.fragments.LoginWithPwdFragment
import cn.troph.tomon.ui.fragments.PHONE_CODE_TYPE
import cn.troph.tomon.ui.fragments.PHONE_CODE_TYPE_LOGIN
import com.gyf.immersionbar.ImmersionBar

class OptionalLoginActivity : AppCompatActivity() {

    data class UserInfo(
        var phone: String? = null,
        var pwd: String? = null,
        var verification: String? = null,
        var activateCode: String? = null
    )
    val userInfo = UserInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).reset().init()
        setContentView(R.layout.login_activity)
        intent.extras?.let {
            if (it.getInt("login_type") == LOGIN_NUMBER) {
                val loginNumberFragment = LoginWithNumberFragment()
                loginNumberFragment.arguments = Bundle().apply {
                    putInt(PHONE_CODE_TYPE, PHONE_CODE_TYPE_LOGIN)
                }
                fragmentAdd(loginNumberFragment)
            } else if (it.getInt("login_type") == LOGIN_PASSWORD) {
                val loginPwdFragment = LoginWithPwdFragment()
                fragmentAdd(loginPwdFragment)
            } else {
                val intent = Intent(this, EntryOptionActivity::class.java)
                startActivity(intent)
            }

        }


    }

    private fun fragmentAdd(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.content, fragment)
        }.commit()
    }
}