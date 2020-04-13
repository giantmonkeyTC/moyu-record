package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable

class MessageCollection(client: Client, private val channelId: String) :
    BaseCollection<Message>(client) {

    val channel get() = client.channels.get(channelId)
    var gotBeginning: Boolean = false
    var latestMessage: Message? = null
    var earliestMessage: Message? = null
    val sorted: MutableList<String> = mutableListOf()

    override fun add(data: JsonObject, identify: ((d: JsonObject) -> String)?): Message? {
        val message = super.add(data, identify ?: {
            val id = it["id"] as String
            val nonce = it["nonce"] as String
            if (get("N$nonce") != null) {
                "N$nonce"
            } else {
                id
            }
        })
        if (message != null) {
            val index = sorted.binarySearchBy(message) { id ->
                get(id)
            }
            sorted.add(index, message.id)
        }
        return message
    }

    override fun remove(key: String): Message? {
        val message = get(key)
        if (message != null) {
            val index = sorted.binarySearchBy(message) { id ->
                get(id)
            }
            sorted.removeAt(index)
        }
        return super.remove(key)
    }

    override fun instantiate(data: JsonObject): Message? {
        return Message(client, data)
    }

    private fun updateRange(messages: List<Message>) {
        if (messages.size > 0) {
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
//        client.rest.messageService.getMessage(channelId, id, client.token).
        return Observable.empty()
    }

    fun fetch(
        beforeId: String? = null,
        afterId: String? = null,
        limit: Int = 50
    ): Observable<Message> {
        return Observable.empty()
    }

}