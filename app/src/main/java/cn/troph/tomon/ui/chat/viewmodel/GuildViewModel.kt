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

class GuildViewModel : ViewModel() {


    private val guildListLiveData: MutableLiveData<MutableList<Guild>> = MutableLiveData()

    fun getGuildListLiveData(): MutableLiveData<MutableList<Guild>> {
        return guildListLiveData
    }

    fun loadGuildList() {
        Client.global.guilds.fetch(false).observeOn(AndroidSchedulers.mainThread()).subscribe(
            Consumer {
                it?.let {
                    guildListLiveData.value = it.toMutableList()
                }
            })
        Client.global.guilds.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                it?.let {
                    val event = it
                    event.obj?.let {
                        val g = it
                        if (event.type == EventType.SET) {
                            guildListLiveData.value?.add(it)
                            guildListLiveData.notifyObserver()
                        }
                        if (event.type == EventType.REMOVE) {
                            guildListLiveData.value?.removeIf {
                                it.id == g.id
                            }
                        }
                    }
                }
            })
    }

}