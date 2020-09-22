package cn.troph.tomon.ui.activities


import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_channel_detail.*
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import kotlinx.android.synthetic.main.partial_chat_app_bar.*

class ChatActivity : BaseActivity() {
    private lateinit var mCurrentChannel: Channel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val mChatSharedViewModel: ChatSharedViewModel by viewModels()
        val map = HashMap<String, Int>()
        val bundle = intent.extras
        bundle?.let {
            mChatSharedViewModel.channelSelectionLD.value = ChannelSelection(
                guildId = bundle.getString("guildId"),
                channelId = bundle.getString("channelId")
            )
        }




        Client.global.dmChannels.forEach {
            map[it.id] = it.unReadCount
        }

        setSupportActionBar(toolbar)
        text_toolbar_title.text = getString(R.string.app_name_capital)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon = getDrawable(R.drawable.channel_info_back)

        mChatSharedViewModel.channelSelectionLD.observe(this, Observer {
            if (it.channelId != null) {
                val channel = Client.global.channels[it.channelId]
                channel?.let {
                    mCurrentChannel = it
                    updateToolbarAndInputEditTextView(it)
                }
            }
        })
        mChatSharedViewModel.syncMessageLD.observe(this, Observer {
            if (it is TextChannel) {
                it.messages.fetch(limit = 50).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe({

                    }, {

                    })
            } else if (it is DmChannel) {
                it.messages.fetch(limit = 50).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe({

                    }, {

                    })
            }

        })
        mChatSharedViewModel.channelUpdateLD.observe(this, Observer {
            if (it.channel.id == AppState.global.channelSelection.value.channelId) {
                updateToolbarAndInputEditTextView(it.channel)
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


    private fun updateToolbarAndInputEditTextView(channel: Channel) {
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
            editText.setHint(getString(R.string.emoji_et_hint))
            enableInputPanel()
        } else if (channel is DmChannel) {
            text_toolbar_title.text = channel.recipient?.name
            image_toolbar_icon.setImageResource(R.drawable.ic_channel_text_unlock)
            editText.setHint(getString(R.string.emoji_et_hint))
            enableInputPanel()
        }
    }

    private fun enableInputPanel() {
        btn_message_menu.isEnabled = true
        emoji_tv.isEnabled = true
        editText.isEnabled = true
        btn_message_send.isClickable = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (intent.extras?.get("guildId") as String == "@me") {
            menuInflater.inflate(R.menu.dmchannel_temporary_menu, menu)
            return true
        } else {
            menuInflater.inflate(R.menu.activity_chat, menu)
            return true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right_custom)
        hideKeyboard()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right_custom)
                hideKeyboard()
            }
            R.id.members -> {
                    val intent = Intent(this, ChannelInfoActivity::class.java)
                    val bundle = Bundle()
                    bundle.putString("guildId", getIntent().extras?.getString("guildId"))
                    bundle.putString("channelId", mCurrentChannel.id)
                    intent.putExtras(bundle)
                    startActivity(
                        intent,
                        ActivityOptions.makeCustomAnimation(
                            this,
                            R.anim.slide_in_right_custom,
                            R.anim.no_animation
                        ).toBundle()
                    )

                    hideKeyboard()
            }

        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}