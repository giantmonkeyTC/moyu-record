package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.MessageFetchEvent
import cn.troph.tomon.core.network.services.MessageService
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.core.utils.SortedList
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.net.URL

class MessageCollection(client: Client, val channel: Channel) :
    BaseCollection<Message>(client) {

    var gotBeginning: Boolean = false
    var latestMessage: Message? = null
    var earliestMessage: Message? = null

    // 因为消息拥有两个id，id和nonce，两者会变化，不确定使用哪一个参与排序，所以这里使用插入时的id保持一致
    // 如果是使用nonce作为id，后面会加N作为区分
    private val sortedList: SortedList<String> =
        SortedList(Comparator { o1, o2 -> o1.compareTo(o2) })

    private val list get() = sortedList

    fun getSortedList(): MutableList<Message> {
        val cloneMap = this.clone().toMap()
        val sorted = mutableListOf<Message>()
        list.forEach {
            cloneMap[it.substring(2)]?.let {
                sorted.add(it)
            }
        }
        return sorted
    }

    override fun add(data: JsonObject, identify: CollectionIdentify?): Message? {
        val advancedId = identify ?: {
            val id = it["id"].optString
            val nonce = it["nonce"].optString
            // 如果原来按nonce加入了，则优先查找nonce的
            if (nonce != null && get(Message.getNonceId(nonce)) != null) {
                Message.getNonceId(nonce)
            } else {
                id
            }
        }
        // 检查之前是否加过
        val exist = advancedId(data).let { if (it != null) get(it) else null }
        // 添加
        val message = super.add(data, advancedId)
        // 如果之前按照nonce添加到collection里，如果这里有了id，按照id加一下
        val id = message?.id
        if (id != null && get(id) == null) {
            put(id, message)
        }
        // 如果第一次加，加入排序
        if (exist == null && message != null) {
            sortedList.addIfNotExist(message.sortKey)
        }
        return message
    }

    override fun remove(key: String): Message? {
        val message = get(key)
        if (message != null) {
            // 如果有nonce(只有自己的才会)，移除一下nonce的
            if (message.nonce != null) {
                val m = super.remove(message.nonceId)
                // 如果是nonce添加，则一定会用nonce排序，使用nonce排序删除
                if (m != null) {
                    sortedList.remove(m.nonceSortKey)
                }
            }
            // 如果有id
            if (message.sortKey != message.nonceSortKey) {
                sortedList.remove(message.sortKey)
            }
        }
        return super.remove(key)
    }

    override fun instantiate(data: JsonObject): Message? {
        return Message(client, data)
    }

    private fun updateRange(messages: List<Message>) {
        if (messages.isNotEmpty()) {
            val early = messages[0];
            val late = messages[messages.size - 1];
            if (earliestMessage == null) {
                earliestMessage = early;
            } else {
                if (early < earliestMessage!!) {
                    earliestMessage = early;
                }
            }
            if (latestMessage == null) {
                latestMessage = late
            } else {
                if (late > latestMessage!!) {
                    latestMessage = late
                }
            }
        }
    }

    fun fetchOne(id: String): Observable<Message> {
        return client.rest.messageService.getMessage(channel.id, id, client.auth)
            .subscribeOn(Schedulers.io()).map {
                return@map client.actions.messageFetch(it)?.get(0)
            }
    }

    fun fetch(
        beforeId: String? = null,
        afterId: String? = null,
        limit: Int = 50
    ): Observable<List<Message>> {
        return client.rest.messageService.getMessages(
            channel.id,
            beforeId,
            afterId,
            limit,
            client.auth
        ).subscribeOn(Schedulers.io()).map {
            if (it.size() < limit) {
                client.actions.messageFetch(it, true, channel.id)
            } else {
                client.actions.messageFetch(it)
            }
        }
    }

    fun fetchWithoutStorage(
        beforeId: String? = null,
        afterId: String? = null,
        limit: Int = 50
    ): Observable<List<Message>> {
        return client.rest.messageService.getMessages(
            channel.id,
            beforeId,
            afterId,
            limit,
            client.auth
        ).subscribeOn(Schedulers.io()).map {
            if (it.size() < limit) {
                parseMessages(it, true, channel.id)
            } else {
                parseMessages(it)
            }
        }
    }

    fun parseMessages(
        data: JsonElement,
        gotBeginning: Boolean? = false,
        channelId: String? = null
    ): List<Message>? {
        val getChannelId = { data: JsonElement ->
            if (data.isJsonArray) {
                data.asJsonArray.get(0)?.asJsonObject?.get("channel_id")?.optString
            } else {
                data.asJsonObject["channel_id"].optString
            }
        }
        val tempChannelId = channelId as? String ?: (getChannelId(data!!) ?: "")
        val channel =
            (if (tempChannelId == "") null else client.channels[tempChannelId ?: ""]) ?: return null
        if (channel !is TextChannelBase) {
            return null
        }
        val messages = mutableListOf<Message>()
        val parse = { obj: JsonObject ->
            val message = Message(client, obj)
            if (message != null) {
                messages.add(message)
            }
        }
        if (data!!.isJsonArray) {
            data!!.asJsonArray.forEach { parse(it.asJsonObject) }
        } else {
            parse(data!!.asJsonObject)
        }
        if (messages.isNotEmpty()) {
            client.eventBus.postEvent(MessageFetchEvent(messages))
        }
        if (gotBeginning != false) {
            channel.messages.gotBeginning = true
        }
        return messages
    }

    fun create(content: String, nonce: String): Observable<Unit> {
        return client.rest.messageService.createMessage(
            channel.id,
            MessageService.CreateMessageRequest(content, nonce),
            client.auth
        )
            .subscribeOn(Schedulers.io()).map {

            }
    }

    fun uploadAttachments(
        partMap: Map<String, RequestBody>,
        files: MultipartBody.Part
    ): Observable<Message> {
        return client.rest.messageService.uploadAttachments(
            channelId = channel.id,
            partMap = partMap,
            files = files,
            token = client.auth
        ).subscribeOn(Schedulers.io()).map {
            client.actions.messageCreate(it)
        }
    }
}

