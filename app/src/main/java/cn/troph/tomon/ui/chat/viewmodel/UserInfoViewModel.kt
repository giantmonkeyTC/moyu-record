package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Me
import cn.troph.tomon.core.structures.User
import cn.troph.tomon.ui.chat.messages.notifyObserver
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class UserInfoViewModel : ViewModel() {
    private val userInfoLiveData = MutableLiveData<Me>()
    val guildUserInfoLD = MutableLiveData<User>()


    fun getUserInfoLiveData() = userInfoLiveData


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

}