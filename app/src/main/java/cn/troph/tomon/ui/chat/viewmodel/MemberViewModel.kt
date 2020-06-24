package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Role
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.ui.chat.messages.notifyObserver
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class MemberViewModel : ViewModel() {
    private val memberLiveData: MutableLiveData<MutableList<GuildMember>> = MutableLiveData()
    fun getMembersLiveData() = memberLiveData

    fun loadMemberList(channelId: String) {
        val channel = Client.global.channels[channelId] as TextChannel
        memberLiveData.value = channel.members.sortedMemberList().toMutableList()
        channel.members.observable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            it.obj?.let {
                memberLiveData.value?.add(it)
                memberLiveData.notifyObserver()
            }
        }
    }

}