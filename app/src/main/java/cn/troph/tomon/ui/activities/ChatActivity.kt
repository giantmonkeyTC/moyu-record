package cn.troph.tomon.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.troph.tomon.R

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        println("chat activity!!!!!")
    }

}