package cn.troph.tomon.ui.activities

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle

class JoinGuildDeepLinkActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (intent!=null){
            val uri = intent.data
            uri?.let {
                val scheme = it.scheme
                val host = it.host
            }

        }
    }
}