package cn.troph.tomon.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.R
import cn.troph.tomon.ui.chat.fragments.ChannelSelectorFragment

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val selectorFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_channel_selector) as? ChannelSelectorFragment
        println(selectorFragment?.javaClass)
    }

}