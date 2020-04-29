package cn.troph.tomon.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import cn.troph.tomon.R
import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.ui.states.AppState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class ChatActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val toolbarTitle: TextView = toolbar.findViewById(R.id.text_toolbar_title)
        setSupportActionBar(toolbar)
        toolbarTitle.text = "TOMON"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon = getDrawable(R.drawable.ic_channel_selector)

        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.channelId != null) {
                    val channel = Client.global.channels[it.channelId]
                    if (channel != null) {
                        updateToolbar(channel)
                    }
                }
            }
    }

    private fun updateToolbar(channel: Channel) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val toolbarTitle: TextView = toolbar.findViewById(R.id.text_toolbar_title)
        val toolbarImage: ImageView = toolbar.findViewById(R.id.image_toolbar_icon)
        if (channel is GuildChannel) {
            var iconId: Int? = null
            toolbarTitle.text = channel.name
            when (channel.type) {
                ChannelType.TEXT -> {
                    iconId =
                        if (channel.isPrivate) R.drawable.ic_channel_text_lock else R.drawable.ic_channel_text
                }
                ChannelType.VOICE -> {
                    iconId =
                        if (channel.isPrivate) R.drawable.ic_channel_voice_lock else R.drawable.ic_channel_voice
                }
            }
            if (iconId != null) {
                toolbarImage.setImageResource(iconId)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
                drawerLayout.openDrawer(GravityCompat.START, true)
            }
            R.id.members -> {
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
                drawerLayout.openDrawer(GravityCompat.END, true)
            }
        }
        return true
    }

}