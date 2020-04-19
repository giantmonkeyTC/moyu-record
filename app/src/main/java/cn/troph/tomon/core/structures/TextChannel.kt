package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageNotificationsType
import cn.troph.tomon.core.collections.MessageCollection
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
import java.time.LocalDateTime

class TextChannel(client: Client, data: JsonObject) : GuildChannel(client, data), TextChannelBase {

    var topic: String? = null
        private set
    override var lastMessageId: String? = null
        private set
    override var ackMessageId: String? = null
        private set
    override var localAckMessageId: String? = null
        private set
    var defaultMessageNotifications: MessageNotificationsType = MessageNotificationsType.DEFAULT
        private set

    override val messages: MessageCollection = MessageCollection(client, this)
    override val typings: Collection<LocalDateTime> = Collection()

    override val unread get() = getUnread()
    override val messageNotifications get() = getMessageNotifications()
    override val muted get() = getMuted()

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("topic")) {
            topic = data["topic"].asString
        }
        if (data.has("last_message_id")) {
            lastMessageId = data["last_message_id"].optString
        }
        if (data.has("ack_message_id")) {
            ackMessageId = data["ack_message_id"].optString
            localAckMessageId = ackMessageId
        }
        if (data.has("default_message_notifications")) {
            val value = data["default_message_notifications"].asInt
            defaultMessageNotifications =
                MessageNotificationsType.fromInt(value) ?: MessageNotificationsType.DEFAULT
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

}