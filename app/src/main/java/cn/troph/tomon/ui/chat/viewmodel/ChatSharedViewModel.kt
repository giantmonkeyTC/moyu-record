package cn.troph.tomon.ui.chat.viewmodel

import android.text.BoringLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.*
import cn.troph.tomon.core.structures.*
import cn.troph.tomon.core.utils.event.observeEventOnUi
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.ChannelSelection
import cn.troph.tomon.ui.states.UpdateEnabled
import com.google.android.gms.common.api.Api
import com.google.gson.annotations.SerializedName
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ChatSharedViewModel : ViewModel() {
    val voiceMicControllerLD = MutableLiveData<Boolean>()
    val voiceSoundControllerLD = MutableLiveData<Boolean>()
    val voiceEarPhoneControllerLD = MutableLiveData<Boolean>()
    val voiceLeaveControllerLD = MutableLiveData<Boolean>()
    val voiceGuildVoiceEnableLD = MutableLiveData<GuildChannel>()
    val voiceGuildVoiceDisableLD = MutableLiveData<Boolean>()

    val voiceMicState = MutableLiveData<Boolean>()
    val voiceSoundState = MutableLiveData<Boolean>()
    val voiceSpeakerState = MutableLiveData<Boolean>()


    val channelSelectionLD = MutableLiveData<ChannelSelection>()
    val upEventDrawerLD = MutableLiveData<Any>()
    val channelCollapses = MutableLiveData<Map<String, Boolean>>()
    val channelUpdateLD = MutableLiveData<ChannelUpdateEvent>()

    val messageLiveData = MutableLiveData<MutableList<Message>>()

    val messageMoreLiveData = MutableLiveData<MutableList<Message>>()

    val messageLoadingLiveData = MutableLiveData<Boolean>()

    val updateLD = MutableLiveData<UpdateEnabled>()

    val messageCreateLD: MutableLiveData<MessageCreateEvent> = MutableLiveData()

    val reactionAddLD = MutableLiveData<ReactionAddEvent>()

    val reactionRemoveLD = MutableLiveData<ReactionRemoveEvent>()

    val messageDeleteLD: MutableLiveData<MessageDeleteEvent> = MutableLiveData()

    val messageUpdateLD: MutableLiveData<MessageUpdateEvent> = MutableLiveData()

    val mChannelCreateLD = MutableLiveData<ChannelCreateEvent>()

    val dmChannelLiveData = MutableLiveData<MutableList<DmChannel>>()

    val memberLiveData: MutableLiveData<MutableList<GuildMember>> = MutableLiveData()

    val dmMemberLiveData: MutableLiveData<MutableList<User>> = MutableLiveData()

    val presenceUpdateLV = MutableLiveData<PresenceUpdateEvent>()

    val userInfoLiveData = MutableLiveData<Me>()

    val guildUserInfoLD = MutableLiveData<User>()

    val dmUnReadLiveData = MutableLiveData<HashMap<String, Int>>()

    val messageReadLD: MutableLiveData<MessageReadEvent> = MutableLiveData()

    val messageAtMeLD: MutableLiveData<MessageAtMeEvent> = MutableLiveData()

    val guildListLiveData: MutableLiveData<MutableList<Guild>> = MutableLiveData()

    val guildPositionLD: MutableLiveData<GuildPositionEvent> = MutableLiveData()

    val guildCreateLD: MutableLiveData<GuildCreateEvent> = MutableLiveData()

    val guildDeleteLD: MutableLiveData<GuildDeleteEvent> = MutableLiveData()

    fun setUpEvents() {

        AppState.global.channelSelection.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                channelSelectionLD.value = it
            })

        AppState.global.eventBus.observeEventsOnUi().subscribe(Consumer {
            upEventDrawerLD.value = it
        })

        AppState.global.updateEnabled.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                updateLD.value = it
            })

        Client.global.eventBus.observeEventOnUi<MessageAtMeEvent>().subscribe(Consumer {
            messageAtMeLD.value = it
        })

        Client.global.eventBus.observeEventOnUi<ChannelUpdateEvent>().subscribe(Consumer {
            channelUpdateLD.value = it
        })

        Client.global.eventBus.observeEventOnUi<ChannelDeleteEvent>().subscribe(Consumer {
            if (it.channel is DmChannel) {
                dmUnReadLiveData.value?.remove(it.channel.id)
                dmUnReadLiveData.notifyObserver()
            }
        })

        Client.global.eventBus.observeEventOnUi<MessageCreateEvent>().subscribe(Consumer { event ->
            messageCreateLD.value = event
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
        Client.global.eventBus.observeEventOnUi<MessageDeleteEvent>().subscribe(Consumer {
            messageDeleteLD.value = it
        })

        Client.global.eventBus.observeEventOnUi<MessageUpdateEvent>().subscribe(Consumer {
            messageUpdateLD.value = it
        })
        Client.global.eventBus.observeEventOnUi<ReactionAddEvent>().subscribe(Consumer {
            reactionAddLD.value = it
        })
        Client.global.eventBus.observeEventOnUi<ReactionRemoveEvent>().subscribe(Consumer {
            reactionRemoveLD.value = it
        })
        Client.global.eventBus.observeEventOnUi<ChannelCreateEvent>().subscribe(Consumer {
            mChannelCreateLD.value = it
            if (it.channel is DmChannel) {
                dmUnReadLiveData.value?.put(it.channel.id, it.channel.unReadCount)
                dmUnReadLiveData.notifyObserver()
            }
        })
        Client.global.eventBus.observeEventOnUi<PresenceUpdateEvent>().subscribe(Consumer {
            presenceUpdateLV.value = it
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
        Client.global.eventBus.observeEventOnUi<MessageReadEvent>().subscribe(Consumer {
            messageReadLD.value = it
        })
        Client.global.eventBus.observeEventOnUi<MessageAtMeEvent>().subscribe(Consumer {
            messageAtMeLD.value = it
        })
    }


    fun loadTextChannelMessage(channelId: String) {
        messageLoadingLiveData.value = true
        val channel = Client.global.channels[channelId] as TextChannel
        channel.messages.fetch(limit = 50).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    messageLoadingLiveData.value = false
                    messageLiveData.value = it.toMutableList()
                }, {
                    messageLoadingLiveData.value = false
                })
    }

    fun loadDmChannelMessage(channelId: String) {
        messageLoadingLiveData.value = true
        val channel = Client.global.channels[channelId] as DmChannel
        channel.messages.fetch(limit = 50).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                messageLoadingLiveData.value = false
                messageLiveData.value = it.toMutableList()
            }, {
                messageLoadingLiveData.value = false
            })
    }

    fun loadDmChannel() {
        dmChannelLiveData.value = Client.global.dmChannels.toMutableList()
    }

    fun loadOldMessage(channelId: String, beforeId: String) {
        val channel = Client.global.channels[channelId]
        if (channel != null) {
            when (channel.type) {
                ChannelType.DM -> (channel as DmChannel).messages.fetch(
                    beforeId = beforeId,
                    limit = 50
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            messageMoreLiveData.value = it.toMutableList()
                        }, {
                            Logger.d(it.message)
                        }
                    )
                ChannelType.TEXT -> (channel as TextChannel).messages.fetch(
                    beforeId = beforeId,
                    limit = 50
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            messageMoreLiveData.value = it.toMutableList()
                        }, {
                            Logger.d(it.message)
                        }
                    )

            }
        }

    }

    fun loadMemberList(channelId: String) {
        val channel = Client.global.channels[channelId] as TextChannel
        val onlineMemberList = mutableListOf<GuildMember>()
        val offlineMemberList = mutableListOf<GuildMember>()
        val presenceMemberList = channel.members.sortedMemberList().toMutableList()
        presenceMemberList.forEach {
            if (Client.global.presences[it.id]?.status == "offline")
                offlineMemberList.add(it)
            else
                onlineMemberList.add(it)
        }
        onlineMemberList.addAll(offlineMemberList)
        memberLiveData.value = onlineMemberList
    }

    fun loadDmMemberList(channelId: String) {
        val channel = Client.global.channels[channelId] as DmChannel
        val dmMembers = mutableListOf<User>()
        val recipient = channel.recipient
        if (recipient != null) {
            dmMembers.add(Client.global.me)
            dmMembers.add(recipient)
        }
        dmMemberLiveData.value = dmMembers
    }

    fun loadUserInfo() {
        userInfoLiveData.value = Client.global.me
        Client.global.me.observable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            userInfoLiveData.value = Client.global.me
            userInfoLiveData.notifyObserver()
        }
    }

    fun loadGuildUserInfo(userId: String) {
        guildUserInfoLD.value = Client.global.users[userId]
    }

    fun loadGuildList() {
        guildListLiveData.value = Client.global.guilds.toMutableList()
    }

}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}


data class GuildUnread(
    @SerializedName("unread") val unread: Boolean,
    @SerializedName("mentionNum") val mentionNum: Int
)


