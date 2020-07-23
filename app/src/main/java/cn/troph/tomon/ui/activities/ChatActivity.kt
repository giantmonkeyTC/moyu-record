package cn.troph.tomon.ui.activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics

import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.view.GravityCompat
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.troph.tomon.R
import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.AppUIEvent
import cn.troph.tomon.ui.states.AppUIEventType
import com.discord.panels.OverlappingPanelsLayout
import com.discord.panels.PanelState
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.partial_chat_app_bar.*

class ChatActivity : BaseActivity() {
    private lateinit var mCurrentChannel: Channel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val mChatSharedViewModel = ViewModelProvider(this).get(ChatSharedViewModel::class.java)

        val map = HashMap<String, Int>()

        Client.global.dmChannels.forEach {
            map[it.id] = it.unReadCount
        }

        setSupportActionBar(toolbar)
        text_toolbar_title.text = getString(R.string.app_name_capital)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon = getDrawable(R.drawable.ic_channel_selector)

        mChatSharedViewModel.channelSelectionLD.observe(this, Observer {
            if (it.channelId != null) {
                val channel = Client.global.channels[it.channelId]
                channel?.let {
                    mCurrentChannel = it
                    updateToolbar(it)
                }
            }
        })
        mChatSharedViewModel.upEventDrawerLD.observe(this, Observer {
            val event = it as? AppUIEvent
            when (event?.type) {
                AppUIEventType.CHANNEL_DRAWER -> {
                    hideKeyboard()
                    val isOpen = event.value as Boolean
                    if (isOpen) overlapping_panels.openStartPanel() else
                        overlapping_panels.closePanels()
                }
                AppUIEventType.MEMBER_DRAWER -> {
                    hideKeyboard()
                    val isOpen = event.value as Boolean
                    if (isOpen) overlapping_panels.openEndPanel() else
                        overlapping_panels.closePanels()
                }
            }
        })
        overlapping_panels.registerStartPanelStateListeners(object :
            OverlappingPanelsLayout.PanelStateListener {
            override fun onPanelStateChange(panelState: PanelState) {
                hideKeyboard()
            }
        })
        overlapping_panels.registerEndPanelStateListeners(object :
            OverlappingPanelsLayout.PanelStateListener {
            override fun onPanelStateChange(panelState: PanelState) {
                hideKeyboard()
            }
        })
        mChatSharedViewModel.channelUpdateLD.observe(this, Observer {
            if (it.channel.id == AppState.global.channelSelection.value.channelId) {
                updateToolbar(it.channel)
            }
        })
        mChatSharedViewModel.setUpEvents()
        mChatSharedViewModel.dmUnReadLiveData.value = map

    }


    private fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setDrawerEdge(mDrawerLayout: DrawerLayout, isLeftDrawer: Boolean = true) {
        try {
            val manager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val width = DisplayMetrics().also { manager.defaultDisplay.getMetrics(it) }.widthPixels

            val viewDragHelper = mDrawerLayout::class.java
                .getDeclaredField(if (isLeftDrawer) "mLeftDragger" else "mRightDragger")
                .apply { isAccessible = true }
                .run { get(mDrawerLayout) as ViewDragHelper }

            viewDragHelper.let { it::class.java.getDeclaredField("mEdgeSize") }
                .apply { isAccessible = true }
                .apply { setInt(viewDragHelper, width / 2) }

        } catch (e: Exception) {
            e.printStackTrace()
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

        } else if (channel is DmChannel) {
            text_toolbar_title.text = channel.recipient?.name
            image_toolbar_icon.setImageResource(R.drawable.ic_channel_text_unlock)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                hideKeyboard()
                overlapping_panels.openStartPanel()
            }
            R.id.members -> {
                if (AppState.global.channelSelection.value.channelId != null) {
                    hideKeyboard()
                    overlapping_panels.openEndPanel()
                }
            }

        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}