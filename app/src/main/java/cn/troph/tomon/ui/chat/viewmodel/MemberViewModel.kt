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
    private val dmMemberLiveData:MutableLiveData<MutableList<User>> = MutableLiveData()
    fun getMembersLiveData() = memberLiveData
    fun getDmMemberLiveData() = dmMemberLiveData

    fun loadMemberList(channelId: String) {
        val channel = Client.global.channels[channelId] as TextChannel
        memberLiveData.value = channel.members.sortedMemberList().toMutableList()
    }

    fun loadDmMemberList(channelId: String) {
        val channel = Client.global.channels[channelId] as DmChannel
        val dmMembers = mutableListOf<User>()
        val recipient = channel.recipient
        if (recipient != null){
            dmMembers.add(recipient)
            dmMembers.add(Client.global.me)
        }
        dmMemberLiveData.value = dmMembers
    }

}