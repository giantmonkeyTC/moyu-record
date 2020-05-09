package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.ui.chat.messages.MessageListAdapter
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_channel_panel.*

class ChannelPanelFragment : Fragment() {

    private var channelId: String? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed && value != null) {
                val channel = Client.global.channels[value]
                if (channel is TextChannelBase) {
                    channel.messages.fetch().observeOn(AndroidSchedulers.mainThread()).subscribe {
                    }
                }
            }
        }
    private var updateEnabled: Boolean = false
        set(value) {
            field = value
            if (field) {
                editText.setText(message?.content)
                bar_update_message.visibility = View.VISIBLE
            } else {
                editText.text = null
                bar_update_message.visibility = View.GONE
            }

        }
    private var message: Message? = null

    init {
        AppState.global.updateEnabled.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                message = it.message
                updateEnabled = it.flag
            }
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                channelId = it.channelId
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<RecyclerView>(R.id.view_messages)
        val bar_update_message = view.findViewById<LinearLayout>(R.id.bar_update_message)
        val btn_send: ImageView = view.findViewById(R.id.btn_message_send)
        val edit_text: EditText = view.findViewById(R.id.editText)
        val btn_update_text_cancel: TextView = view.findViewById(R.id.btn_update_message_cancel)
        btn_send.setOnClickListener {

            if (!AppState.global.updateEnabled.value.flag)
                (Client.global.channels[channelId
                    ?: ""] as TextChannel).messages.create(edit_text.text.toString())
                    .observeOn(AndroidSchedulers.mainThread()).doOnError { error -> println(error) }
                    .subscribe {
                        edit_text.text = null
                    }
            else
                message!!.update(edit_text.text.toString()).observeOn(AndroidSchedulers.mainThread()).doOnError { error ->
                    println(
                        error
                    )
                }.subscribe {
                    AppState.global.updateEnabled.value =
                        UpdateEnabled(flag = false)
                    edit_text.text = null
                }
        }
        list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MessageListAdapter().apply { hasStableIds() }
        }
        btn_update_text_cancel.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = false)
        }


    }

}