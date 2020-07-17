package cn.troph.tomon.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.annotations.SerializedName

class UnReadViewModel : ViewModel() {

    val dmUnReadLiveData = MutableLiveData<HashMap<String, Int>>()
    val guildUnReadLiveData = MutableLiveData<HashMap<String, GuildUnread>>()
    fun setUpLiveData() {
        dmUnReadLiveData.value = HashMap()
        guildUnReadLiveData.value = HashMap()
    }
}

data class GuildUnread(
    @SerializedName("unread") val unread: Boolean,
    @SerializedName("mentionNum") val mentionNum: Int
)