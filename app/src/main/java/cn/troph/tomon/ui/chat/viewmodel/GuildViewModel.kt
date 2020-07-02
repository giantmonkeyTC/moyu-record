package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.Event
import cn.troph.tomon.core.collections.EventType
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.ui.chat.messages.notifyObserver
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

class GuildViewModel : ViewModel() {

    private val guildListLiveData: MutableLiveData<MutableList<Guild>> = MutableLiveData()

    fun getGuildListLiveData(): MutableLiveData<MutableList<Guild>> {
        return guildListLiveData
    }

    fun loadGuildList() {
        guildListLiveData.value = Client.global.guilds.toMutableList()
    }

}