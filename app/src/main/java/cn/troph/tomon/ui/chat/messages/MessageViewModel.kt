package cn.troph.tomon.ui.chat.messages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.Event
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers


fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

class MessageViewModel : ViewModel() {
    private val messageLiveData = MutableLiveData<MutableList<Message>>()

    fun getMessageLiveData(): MutableLiveData<MutableList<Message>> {
        return messageLiveData
    }

    fun loadTextChannelMessage(channelId: String) {
        val channel = Client.global.channels[channelId] as TextChannel
        channel.messages.fetch().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(
                Consumer {
                    messageLiveData.value = it.toMutableList()
                })
    }


}