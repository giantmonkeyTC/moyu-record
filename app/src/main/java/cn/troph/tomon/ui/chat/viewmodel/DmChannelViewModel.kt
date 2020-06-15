package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildChannel

class DmChannelViewModel : ViewModel() {
    private var dmChannelLiveData = MutableLiveData<MutableList<DmChannel>>()

    fun getChannelLiveData(): MutableLiveData<MutableList<DmChannel>> {
        return dmChannelLiveData
    }

    fun loadDmChannel() {
        dmChannelLiveData.value = Client.global.dmChannels.toMutableList()
    }
}