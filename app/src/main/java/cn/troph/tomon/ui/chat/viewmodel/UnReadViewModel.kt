package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.ComputableLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelCreateEvent
import cn.troph.tomon.core.events.ChannelDeleteEvent
import cn.troph.tomon.core.events.MessageCreateEvent
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.chat.messages.notifyObserver
import com.google.android.gms.common.api.Api
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.functions.Consumer

class UnReadViewModel : ViewModel() {

    val dmUnReadLiveData = MutableLiveData<HashMap<String, Int>>()
    val guildUnReadLiveData = MutableLiveData<HashMap<String, GuildUnread>>()

    fun setUpEvents() {
        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>().subscribe(Consumer { event ->
            if (event.message.guild == null) {
                if (event.message.authorId != Client.global.me.id) {
                    dmUnReadLiveData.value?.let { map ->
                        val oldValue = map[event.message.channelId]
                        oldValue?.let {
                            map.replace(event.message.channelId, it + 1)
                            dmUnReadLiveData.notifyObserver()
                        }
                    }
                }
            }
        })
        Client.global.eventBus.observeEventOnUi<ChannelCreateEvent>().subscribe(
            Consumer {
                if (it.channel is DmChannel) {
                    dmUnReadLiveData.value?.put(it.channel.id, it.channel.unReadCount)
                    dmUnReadLiveData.notifyObserver()
                }

            }
        )
        Client.global.eventBus.observeEventOnUi<ChannelDeleteEvent>().subscribe(
            Consumer {
                if (it.channel is DmChannel) {
                    dmUnReadLiveData.value?.remove(it.channel.id)
                    dmUnReadLiveData.notifyObserver()
                }
            }
        )
    }
}

data class GuildUnread(
    @SerializedName("unread") val unread: Boolean,
    @SerializedName("mentionNum") val mentionNum: Int
)