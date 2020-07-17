package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.Event
import cn.troph.tomon.core.collections.EventType
import cn.troph.tomon.core.events.*
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.messages.notifyObserver
import cn.troph.tomon.ui.states.AppState
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

class GuildViewModel : ViewModel() {

    private val guildListLiveData: MutableLiveData<MutableList<Guild>> = MutableLiveData()

    val messageCreateLD: MutableLiveData<MessageCreateEvent> = MutableLiveData()
    val messageReadLD: MutableLiveData<MessageReadEvent> = MutableLiveData()
    val messageAtMeLD: MutableLiveData<MessageAtMeEvent> = MutableLiveData()
    val messageDeleteLD: MutableLiveData<MessageDeleteEvent> = MutableLiveData()
    val messageUpdateLD: MutableLiveData<MessageUpdateEvent> = MutableLiveData()
    val channelCreateLD: MutableLiveData<ChannelCreateEvent> = MutableLiveData()
    val channelDeleteLD: MutableLiveData<ChannelDeleteEvent> = MutableLiveData()
    val guildPositionLD: MutableLiveData<GuildPositionEvent> = MutableLiveData()
    val guildCreateLD: MutableLiveData<GuildCreateEvent> = MutableLiveData()
    val guildDeleteLD: MutableLiveData<GuildDeleteEvent> = MutableLiveData()


    fun getGuildListLiveData(): MutableLiveData<MutableList<Guild>> {
        return guildListLiveData
    }

    fun setUpEventBus() {
        Client.global.eventBus.observeEventOnUi<ChannelDeleteEvent>().subscribe {
            channelDeleteLD.value = it
        }

        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>()
            .subscribe(Consumer {
                messageCreateLD.value = it
            })
        Client.global.eventBus.observeEventOnUi<MessageReadEvent>()
            .subscribe(Consumer {
                messageReadLD.value = it
            })
        Client.global.eventBus.observeEventOnUi<MessageAtMeEvent>().subscribe(Consumer {
            messageAtMeLD.value = it
        })
        Client.global.eventBus.observeEventOnUi<MessageDeleteEvent>().subscribe(
            Consumer {
                messageDeleteLD.value = it
            }
        )
        Client.global.eventBus.observeEventOnUi<MessageUpdateEvent>().subscribe(Consumer {
            messageUpdateLD.value = it
        })
        Client.global.eventBus.observeEventOnUi<ChannelCreateEvent>().subscribe(Consumer {
            channelCreateLD.value = it
        })
        Client.global.eventBus.observeEventOnUi<GuildPositionEvent>().subscribe(
            Consumer {
                guildPositionLD.value = it
            }
        )
        Client.global.eventBus.observeEventOnUi<GuildCreateEvent>().subscribe(
            Consumer {
                guildCreateLD.value = it
            }
        )
        Client.global.eventBus.observeEventOnUi<GuildDeleteEvent>().subscribe(Consumer {
            guildDeleteLD.value = it
        })
    }

    fun loadGuildList() {
        guildListLiveData.value = Client.global.guilds.toMutableList()


    }

}