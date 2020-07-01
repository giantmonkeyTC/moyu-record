package cn.troph.tomon.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.emoji.widget.EmojiEditText
import cn.troph.tomon.R
import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.fragments.LAST_CHANNEL_ID
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.AppUIEvent
import cn.troph.tomon.ui.states.AppUIEventType
import cn.troph.tomon.ui.states.ChannelSelection
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.partial_chat_app_bar.*
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    private lateinit var mMenu: Menu

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
        Thread.sleep(1000)
    }

    private val mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        text_toolbar_title.text = getString(R.string.app_name_capital)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon = getDrawable(R.drawable.ic_channel_selector)
        AppState.global.channelSelection.observable.delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
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
                        if (channel.isPrivate) R.drawable.ic_channel_text_lock else R.drawable.ic_channel_text_unlock
                }
                ChannelType.VOICE -> {
                    iconId =
                        if (channel.isPrivate) R.drawable.ic_channel_voice_lock else R.drawable.ic_channel_voice
                }
            }
            if (iconId != null) {
                image_toolbar_icon.setImageResource(iconId)
            }
            mHandler.postDelayed({ mMenu?.findItem(R.id.members)?.isVisible = true }, 1000)

        } else if (channel is DmChannel) {
            text_toolbar_title.text = channel.recipient?.name
            image_toolbar_icon.setImageResource(R.drawable.ic_channel_text_unlock)
            mHandler.postDelayed({ mMenu?.findItem(R.id.members)?.isVisible = false }, 1000)
        }
    }

    //用于判断是否要收起键盘
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            val view = this.currentFocus

            if (isShouldHideInput(view, ev)) {
                hideKeyboard(this)
            }
        }
        return super.dispatchTouchEvent(ev)
    }


    private fun isShouldHideInput(view: View?, motionEvent: MotionEvent): Boolean {
        if (view?.id == R.id.btn_message_send) {
            return false
        }
        if (view != null && (view is EditText || view is EmojiEditText)) {
            val l = arrayOf(0, 0)
            val left = l[0]
            val top = l[1]
            val bottom = top + view.height
            val right = left + view.width
            if (motionEvent.x > left && motionEvent.x < right && motionEvent.y > top && motionEvent.y < bottom) {
                return false
            } else {
                return true
            }
        }
        return false
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_chat, menu)
        menu?.let {
            mMenu = it
        }
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}