package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelCreateEvent
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.utils.event.observeEventOnUi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

class DmChannelViewModel : ViewModel() {
    private var dmChannelLiveData = MutableLiveData<MutableList<DmChannel>>()
    val mChannelCreateLD = MutableLiveData<ChannelCreateEvent>()
    val mMessageCreateLD = MutableLiveData<MessageCreateEvent>()

    fun getChannelLiveData(): MutableLiveData<MutableList<DmChannel>> {
        return dmChannelLiveData
    }

    fun loadDmChannel() {
        dmChannelLiveData.value = Client.global.dmChannels.toMutableList()
    }

    fun setUpEvents() {
        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>().subscribe(Consumer {
            mMessageCreateLD.value = it
        })
        Client.global.eventBus.observeEventOnUi<ChannelCreateEvent>().subscribe(Consumer {
            mChannelCreateLD.value = it
        })
    }
}