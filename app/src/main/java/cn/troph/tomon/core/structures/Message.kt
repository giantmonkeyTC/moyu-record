package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageType
import cn.troph.tomon.core.collections.MessageReactionCollection
import cn.troph.tomon.core.events.MessageReplySourceReadyEvent
import cn.troph.tomon.core.network.services.MessageService
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.Converter
import cn.troph.tomon.core.utils.optString
import cn.troph.tomon.core.utils.snowflake
import cn.troph.tomon.ui.chat.fragments.Invite
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime

open class Message(client: Client, data: JsonObject) : Base(client, data),
    Comparable<Message> {

    companion object {
        fun getNonceId(nonce: String): String = "${nonce}N"
    }

    var isSending = false

    // 发送中的message id是空的
    var id: String? = null
        private set
    var channelId: String = ""
        private set
    var authorId: String? = null
        private set
    var type: MessageType = MessageType.DEFAULT
        private set
    var content: String? = null
        private set
    var timestamp: LocalDateTime = LocalDateTime.now()
        private set
    var nonce: String? = null
        private set
    var pending: Boolean = false
        private set
    var attachments: Collection<MessageAttachment> = Collection()
        private set
    var mentions: Collection<User> = Collection()
        private set
    var reply: Reply? = null

    var editedTimestamp: LocalDateTime? = null

    val reactions: MessageReactionCollection = MessageReactionCollection(client, this)

    val stamps = mutableListOf<Stamp>()

    val links = mutableListOf<Link>()

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("channel_id")) {
            channelId = data["channel_id"].asString
        }
        if (data.has("type")) {
            val value = data["type"].asInt
            type = MessageType.fromInt(value) ?: MessageType.DEFAULT
        }
        if (data.has("author") && !data["author"].isJsonNull) {
            val author = client.users.add(data["author"].asJsonObject)
            authorId = author?.id
        }
        if (data.has("content")) {
            content = data["content"].optString
        }
        if (data.has("timestamp")) {
            timestamp = Converter.toDate(data["timestamp"].asString)
        }
        if (data.has("nonce")) {
            nonce = data["nonce"].optString
        }
        if (data.has("edited_timestamp")) {
            if (data["edited_timestamp"] !is JsonNull) {
                editedTimestamp = Converter.toDate(data["edited_timestamp"].asString)
            }
        }
        if (data.has("attachments")) {
            attachments = Collection()
            val array = data["attachments"].asJsonArray
            array.forEach { ele ->
                val at = ele.asJsonObject
                attachments.put(
                    at["id"].asString,
                    MessageAttachment(client, at)
                )
            }
        }
        if (data.has("reactions")) {
            val array = data["reactions"].asJsonArray
            reactions.clear()
            array.forEach { ele ->
                val obj = ele.asJsonObject
                reactions.add(obj)
            }
        }
        if (data.has("mentions")) {
            val array = data["mentions"].asJsonArray
            array.forEach { ele ->
                val u = ele.asJsonObject
                val user = client.users.add(u)
                if (user != null) {
                    mentions.put(user.id, user)
                }
            }
        }
        if (data.has("pending")) {
            pending = data["pending"].asBoolean
        }

        if (data.has("stamps")) {
            stamps.clear()
            stamps.addAll(
                Gson().fromJson(data.get("stamps"), Array<Stamp>::class.java).toMutableList()
            )
        }
        if (data.has("reply")) {
            reply = null
            reply = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                .fromJson(data.get("reply"), Reply::class.java)
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    // 排序用的key
    val sortKey get() = id?.snowflake?.aligned ?: nonceSortKey
    val nonceSortKey
        get() = when (nonce?.snowflake?.aligned) {
            null -> ""
            else -> nonce?.snowflake?.aligned + "N"
        }

    var replySource: Message? = null
        get() =
            reply?.let {
                if (channel == null) {
                    return null
                }

                if ((channel as TextChannelBase).messages.has(it.id))
                    (channel as TextChannelBase).messages[it.id]
                else {
                    (channel as TextChannelBase).messages.fetchOne(it.id).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe {
                            Client.global.eventBus.postEvent(MessageReplySourceReadyEvent(it,id))
                        }
                    return@let Message(client, JsonObject().apply {
                        addProperty("content", "")
                        addProperty("id", it.id)
                        val userObject = JsonObject()
                        userObject.addProperty("id", it.author.id)
                        userObject.addProperty("username", it.author.username)
                        userObject.addProperty(
                            "discriminator",
                            it.author.discriminator
                        )
                        userObject.addProperty("name", it.author.name)
                        userObject.addProperty("avatar", it.author.avatar)
                        userObject.addProperty("avatar_url", it.author.avatarURL)
                        add("author", userObject)
                    })
                }
            }

    // 唯一确定用的id
    val nonceId get() = getNonceId(nonce ?: "")

    val author: User?
        get() {
            val authorId = this.authorId
            return if (authorId != null) client.users.get(authorId) else null
        }

    val channel get() = client.channels.get(channelId)

    val guild get() : Guild? = if (this.channel is GuildChannel) (this.channel as GuildChannel).guild else null

    override fun compareTo(other: Message): Int {
        return sortKey.compareTo(other.sortKey)
    }

    fun delete(): Observable<Message> {
        return client.rest.messageService.deleteMessage(this.channelId, this.id, client.auth)
            .doOnError { error -> println(error) }.subscribeOn(Schedulers.io()).map {
                client.actions.messageDelete(this.raw)
                this
            }
    }

    fun update(content: String): Observable<Message> {
        return client.rest.messageService.updateMessage(
            this.channelId,
            this.id,
            MessageService.UpdateMessageRequest(content),
            client.auth
        ).subscribeOn(Schedulers.io()).map {
            client.actions.messageUpdate(it)
        }
    }

    fun ack(): Observable<String> {
        return client.rest.messageService.ackMessage(
            this.channelId,
            this.id,
            client.auth
        ).subscribeOn(Schedulers.io()).map {
            this.id
        }
    }

}


data class Stamp(
    @SerializedName("alias") val alias: String,
    @SerializedName("animated") val animated: Boolean,
    @SerializedName("author_id") val author_id: String,
    @SerializedName("hash") val hash: String,
    @SerializedName("height") val height: Int = 0,
    @SerializedName("width") val width: Int = 0,
    @SerializedName("id") val id: String,
    @SerializedName("pack_id") val pack_id: String,
    @SerializedName("position") val position: Int,
    @SerializedName("updated_at") val updatedAt: String
)

data class Link(
    @SerializedName("message_id") val messageId: String,
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("img") val img: String?,
    @SerializedName("position") val position: Int,
    @SerializedName("code") val code: Int
)

data class Reply(
    @Expose
    @SerializedName("id") val id: String,
    @Expose
    @SerializedName("channel_id") val channelId: String,
    @Expose
    @SerializedName("author") val author: User
)

class HeaderMessage(
    client: Client,
    obj: JsonObject,
    var isEnd: Boolean = false,
    var isGuild: Boolean = true,
    var channelText: String = ""
) :
    Message(client, obj)