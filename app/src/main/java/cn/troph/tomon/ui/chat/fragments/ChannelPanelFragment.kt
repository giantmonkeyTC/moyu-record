package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.ui.states.AppState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

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

    init {
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
}