package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildFetchEvent
import cn.troph.tomon.core.events.GuildSyncEvent
import cn.troph.tomon.core.utils.event.observeEventOnUi
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class DataPullingViewModel : ViewModel() {

    val dataFetchLD = MutableLiveData<Boolean>()

    fun setUpFetchData() {
        Client.global.eventBus.observeEventOnUi<GuildSyncEvent>().subscribe({ event ->
            dataFetchLD.value = true
        }, {
            dataFetchLD.value = false
        })
    }
}