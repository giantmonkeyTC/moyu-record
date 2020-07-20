package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildFetchEvent
import cn.troph.tomon.core.events.GuildSyncEvent
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.viewmodel.DataPullingViewModel
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import java.util.concurrent.TimeUnit

class EntryActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val dataPullingViewModel: DataPullingViewModel by viewModels()

        dataPullingViewModel.setUpFetchData()

        dataPullingViewModel.dataFetchLD.observe(this, Observer {
            if (it == true)
                gotoChat()
        })
        if (Client.global.loggedIn) {
            gotoChat()
        } else {
            Client.global
                .login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {
                    gotoEntryOption()
                })
        }


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
