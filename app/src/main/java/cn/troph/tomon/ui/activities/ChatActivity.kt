package cn.troph.tomon.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import cn.troph.tomon.R
import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.AppUIEvent
import cn.troph.tomon.ui.states.AppUIEventType
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.partial_chat_app_bar.*

class ChatActivity : AppCompatActivity() {

    init {
        AppState.global.eventBus.observeEventsOnUi().subscribe {
            val event = it as? AppUIEvent
            when (event?.type) {
                AppUIEventType.CHANNEL_DRAWER -> {
                    setChannelDrawerOpen(event.value as Boolean)
                }
                AppUIEventType.MEMBER_DRAWER -> {
                    setMemberDrawerOpen(event.value as Boolean)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        text_toolbar_title.text = "TOMON"
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

        if (channel is GuildChannel) {
            var iconId: Int? = null
            text_toolbar_title.text = channel.name
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
                image_toolbar_icon.setImageResource(iconId)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_chat, menu)
        return true
    }

    private fun setChannelDrawerOpen(open: Boolean) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (open) {
            drawerLayout.openDrawer(GravityCompat.START, true)
        } else {
            drawerLayout.closeDrawer(GravityCompat.START, true)
        }
    }

    private fun setMemberDrawerOpen(open: Boolean) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (open) {
            drawerLayout.openDrawer(GravityCompat.END, true)
        } else {
            drawerLayout.closeDrawer(GravityCompat.END, true)
        }
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

    override fun onDestroy() {
        super.onDestroy()
    }

}