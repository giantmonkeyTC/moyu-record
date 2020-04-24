package cn.troph.tomon.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.R

data class ChatState(val guildId: String? = null, val channelId: String?)

class ChatViewModel : ViewModel() {
    private val _chatState: MutableLiveData<ChatState> by lazy {
        MutableLiveData<ChatState>()
    }
    val chatState: LiveData<ChatState> = _chatState
}

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
    }

}