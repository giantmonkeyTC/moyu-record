package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelUpdateEvent
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import com.google.android.gms.common.api.Api
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import java.util.concurrent.TimeUnit

class ChatSharedViewModel : ViewModel() {

    val channelSelectionLD = MutableLiveData<ChannelSelection>()
    val upEventDrawerLD = MutableLiveData<Any>()
    val channelUpdateLD = MutableLiveData<ChannelUpdateEvent>()

    fun setUpChannelSelection() {
        AppState.global.channelSelection.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                channelSelectionLD.value = it
            })
    }

    fun setUpDrawer() {
        AppState.global.eventBus.observeEventsOnUi().subscribe(Consumer {
            upEventDrawerLD.value = it
        })
    }

    fun setUpChannelUpdate(){
        Client.global.eventBus.observeEventOnUi<ChannelUpdateEvent>().subscribe(Consumer {
            channelUpdateLD.value = it
        })
    }
}