package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageNotificationsType
import cn.troph.tomon.core.collections.MessageCollection
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.Snowflake
import java.time.LocalDateTime

interface TextChannelBase {
    val client: Client

    val lastMessageId: String?
    val ackMessageId: String?
    val localAckMessageId: String?
    val unread: Boolean

    val messages: MessageCollection
    val typings: Collection<LocalDateTime>

    val messageNotifications: MessageNotificationsType
    val muted: Boolean

}

fun TextChannelBase.getUnread(): Boolean {
    return Snowflake.aligned(lastMessageId ?: "0") > Snowflake.aligned(
        localAckMessageId ?: "0"
    )
}

fun TextChannelBase.getMessageNotifications(): MessageNotificationsType =
    MessageNotificationsType.ONLY_MENTION

fun TextChannelBase.getMuted(): Boolean = false

