package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.MessageNotificationsType
import cn.troph.tomon.core.collections.MessageCollection
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.Snowflake
import java.time.LocalDateTime

interface TextChannelBase {
    val lastMessageId: String
    val ackMessageId: String
    val syncAckMessageId: String
    val unread: Boolean

    val messages: MessageCollection
    val typings: Collection<LocalDateTime>

    val messageNotifications: MessageNotificationsType
    val muted: Boolean

    val gotBeginning: Boolean
}

class TextChannelMixin(client: Client, val id: String) : TextChannelBase {
    override var lastMessageId: String = ""
        private set
    override var ackMessageId: String = ""
        private set
    override var syncAckMessageId: String = ""
        private set

    override val messages = MessageCollection(client, id)
    override val typings = Collection<LocalDateTime>(null)

    override val unread
        get() : Boolean = Snowflake.aligned(lastMessageId) > Snowflake.aligned(
            ackMessageId
        )

    override val messageNotifications get() = MessageNotificationsType.ONLY_MENTION
    override val muted get() = false
    override val gotBeginning get() = messages.gotBeginning

    fun patch(data: JsonData) {
        if (data.containsKey("last_message_id")) {
            lastMessageId = data["last_message_id"] as String;
        }
        if (data.containsKey("ack_message_id")) {
            ackMessageId = data["ack_message_id"] as String;
            syncAckMessageId = data["ack_message_id"] as String;
        }
    }
}
