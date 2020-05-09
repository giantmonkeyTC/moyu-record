package cn.troph.tomon.ui.activities

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.ui.chat.messages.MessageListAdapter
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.AppUIEvent
import cn.troph.tomon.ui.states.AppUIEventType
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_channel_panel.*

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

}