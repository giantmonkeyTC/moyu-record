package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.collections.MessageCollection
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
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

    override val messages: MessageCollection = MessageCollection(client, this)
    override val typings: Collection<LocalDateTime> = Collection()

    override val unread get() = getUnread()
    override val messageNotifications get() = getMessageNotifications()
    override val muted get() = getMuted()

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("recipients")) {
            val recipients = data["recipients"].asJsonArray
            if (recipients.size() > 0) {
                val recipient = client.users.add(recipients.get(0).asJsonObject)
                recipientId = recipient?.id ?: ""
            }
        }
        if (data.has("last_message_id")) {
            lastMessageId = data["last_message_id"].optString
        }
        if (data.has("ack_message_id")) {
            ackMessageId = data["ack_message_id"].optString
            localAckMessageId = ackMessageId
        }
    }

    val recipient get() = client.users.get(recipientId)
}