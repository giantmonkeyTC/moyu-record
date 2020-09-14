package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.Url
import cn.troph.tomon.ui.chat.viewmodel.DataPullingViewModel
import cn.troph.tomon.ui.widgets.GeneralSnackbar
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_entry.*

//cn.troph.tomon.ui.activities.EntryActivity
//推送打开的Activity

class EntryActivity : BaseActivity() {

    val function: () -> Unit = {

        val dataPullingViewModel: DataPullingViewModel by viewModels()
        dataPullingViewModel.dataFetchLD.observe(this, Observer {
            if (it == true) {
                invite()
                gotoChannelList()
            }
        })
        dataPullingViewModel.setUpFetchData()
        if (Client.global.loggedIn) {
            invite()
            gotoChannelList()
        } else {
            Client.global
                .login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invite()
                    iv_logo.visibility = View.INVISIBLE
                    iv_people.visibility = View.INVISIBLE
                    rl_root.visibility = View.VISIBLE
                    loading_gif_iv.visibility = View.VISIBLE
                }, {
                    gotoEntryOption()
                })
        }

    }

    fun invite() {
        intent?.let {
            val uri = it.data
            uri?.let {
                mUri->
                if (mUri.toString().contains(Url.inviteUrl)) {
                    Client.global.guilds.fetchInvite(
                        Url.parseInviteCode(mUri.toString())
                    ).observeOn(AndroidSchedulers.mainThread()).subscribe({
                        Logger.d(it.guild.name)
                        if (it != null) {
                            val invite = it
                            if (invite.joined) {
                            } else {
                                Client.global.guilds.join(
                                    Url.parseInviteCode(mUri.toString())
                                )
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                        { guild ->
                                            guild?.let {
                                                Logger.d(it.name)
                                            }

                                        }, { error ->
                                            Logger.d(error.message)
                                        }, { }
                                    )
                            }
                        }
                    }, { error ->
                        Logger.d(error)
                    })
                }
                val a = it
            }
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

    private fun gotoChannelList() {
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
        } catch (e: Exception) {
            // ignore activity is destroyed
        }

    }

}
