package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.MessageCollection
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.optInt
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import java.time.LocalDateTime

class DmChannel(client: Client, data: JsonObject) : Channel(client, data), TextChannelBase {

    var recipientId: String = ""
        private set
    override var lastMessageId: String? = null
        private set
    override var ackMessageId: String? = null
        private set
    override var localAckMessageId: String? = null
        private set

    init {
        patchSelf(data)
    }

    override val messages: MessageCollection = MessageCollection(client, this)
    override val typings: Collection<LocalDateTime> = Collection()

    override val unread get() = getUnread()
    override val messageNotifications get() = getMessageNotifications()
    override val muted get() = getMuted()

    private fun patchSelf(data: JsonObject) {
        if (data.has("recipients")) {
            val recipients = data["recipients"].asJsonArray
            if (recipients.size() > 0) {
                val recipient = client.users.add(recipients.get(0).asJsonObject)
                recipientId = recipient?.id ?: ""
            }
        }
        if (data.has("unread_count")) {
            unReadCount = data["unread_count"].asInt
        }
        if (data.has("last_message_id")) {
            lastMessageId = data["last_message_id"].optString
        }
        if (data.has("ack_message_id")) {
            ackMessageId = data["ack_message_id"].optString
            localAckMessageId = ackMessageId
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    var unReadCount = 0

    val recipient get() = client.users[recipientId]
}