package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.*
import cn.troph.tomon.ui.chat.messages.notifyObserver
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class MemberViewModel : ViewModel() {
    private val memberLiveData: MutableLiveData<MutableList<GuildMember>> = MutableLiveData()
    private val dmMemberLiveData: MutableLiveData<MutableList<User>> = MutableLiveData()
    fun getMembersLiveData() = memberLiveData
    fun getDmMemberLiveData() = dmMemberLiveData

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



}