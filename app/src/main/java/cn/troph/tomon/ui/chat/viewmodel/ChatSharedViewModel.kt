package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import java.util.concurrent.TimeUnit

class ChatSharedViewModel : ViewModel() {

    val channelSelectionLD = MutableLiveData<ChannelSelection>()
    val upEventDrawerLD = MutableLiveData<Any>()

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
}