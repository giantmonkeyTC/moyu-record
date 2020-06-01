package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildChannel

class GuildChannelViewModel : ViewModel() {

    private var guildChannelLiveData = MutableLiveData<MutableList<GuildChannel>>()

    fun getChannelLiveData(): MutableLiveData<MutableList<GuildChannel>> {
        return guildChannelLiveData
    }

    fun loadGuildChannel(guildId: String) {
        val guild = Client.global.guilds[guildId] as Guild
        guildChannelLiveData.value = guild.channels.toMutableList()

    }

}