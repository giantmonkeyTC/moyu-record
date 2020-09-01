package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.chat.viewmodel.DataPullingViewModel
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_entry.*

//cn.troph.tomon.ui.activities.EntryActivity
//推送打开的Activity

class EntryActivity : BaseActivity() {

    val function: () -> Unit = {
        val dataPullingViewModel: DataPullingViewModel by viewModels()
        dataPullingViewModel.dataFetchLD.observe(this, Observer {
            if (it == true)
                gotoChat()
        })
        dataPullingViewModel.setUpFetchData()
        if (Client.global.loggedIn) {
            gotoChat()
        } else {
            Client.global
                .login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    iv_logo.visibility = View.INVISIBLE
                    iv_people.visibility = View.INVISIBLE
                    rl_root.visibility = View.VISIBLE
                    loading_gif_iv.visibility = View.VISIBLE
                }, {
                    gotoEntryOption()
                })
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)
        Glide.with(this).load(R.drawable.loading_splash_gif).into(loading_gif_iv)

        rl_root.postDelayed(function, 2500);

    }

    override fun onDestroy() {
        super.onDestroy()
        rl_root.removeCallbacks(function)
    }

    private fun gotoEntryOption() {
        val intent = Intent(this, EntryOptionActivity::class.java)
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

    private fun gotoChat() {
        try {
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
        } catch (e:Exception) {
            // ignore activity is destroyed
        }

    }

}
