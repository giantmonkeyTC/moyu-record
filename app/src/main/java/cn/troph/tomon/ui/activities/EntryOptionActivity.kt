package cn.troph.tomon.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import cn.troph.tomon.R
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.layout_activity_entry_option.*

class EntryOptionActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_activity_entry_option)
        ImmersionBar.with(this).statusBarColor(R.color.blackPrimary, 0.2f).fitsSystemWindows(true).init()
        button_login_route.setOnClickListener {
            gotoLogin()
        }

        button_register_route.setOnClickListener { gotoRegister() }
    }


    private fun gotoLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(
            intent,
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                register_n_login as View,
                "register_n_login"
            ).toBundle()
        )
    }

    private fun gotoRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(
            intent,
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                register_n_login as View,
                "register_n_login"
            ).toBundle()
        )
    }
}