package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildFetchEvent
import cn.troph.tomon.core.utils.event.observeEventOnUi
import io.reactivex.rxjava3.functions.Consumer


class DataPullingViewModel : ViewModel() {

    val dataFetchLD = MutableLiveData<Boolean>()

    fun setUpFetchData() {

    }
}