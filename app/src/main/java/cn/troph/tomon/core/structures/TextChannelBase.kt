package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageNotificationsType
import cn.troph.tomon.core.collections.MessageCollection
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.snowflake
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
    return (lastMessageId ?: "").snowflake > (localAckMessageId ?: "").snowflake
}

fun getMessageNotificationsForGuild(client: Client, guildId: String): MessageNotificationsType {
    var settings = client.guildSettings[guildId]
    var type = settings?.messageNotifications ?: MessageNotificationsType.DEFAULT
    if (type == MessageNotificationsType.DEFAULT) {
        type = client.guilds[guildId]?.defaultMessageNotifications
            ?: MessageNotificationsType.ONLY_MENTION
    }
    return type
}

fun getMessageNotificationForChannel(client: Client, channelId: String): MessageNotificationsType {
    val channel = client.channels[channelId]
    val guildId = if (channel is GuildChannel) channel.guildId else "@me"
    var settings = client.guildSettings[guildId ?: "@me"]
    var type = settings?.channelOverrides?.get(channelId)?.messageNotifications
        ?: MessageNotificationsType.DEFAULT
    if (type == MessageNotificationsType.DEFAULT) {
        type = if (channel is TextChannel) {
            channel.defaultMessageNotifications
        } else {
            MessageNotificationsType.ALL
        }
    }
    return type
}

fun TextChannelBase.getMessageNotifications(): MessageNotificationsType {
    if (this is GuildChannel) {
        var cursor: GuildChannel? = this
        var type = MessageNotificationsType.DEFAULT
        while (cursor != null && type == MessageNotificationsType.DEFAULT) {
            type = getMessageNotificationForChannel(client, this.id)
            cursor = cursor.parent
        }
        if (type == MessageNotificationsType.DEFAULT) {
            type = getMessageNotificationsForGuild(client, this.guildId ?: "@me")
        }
        return type
    } else if (this is Channel) {
        return getMessageNotificationForChannel(client, this.id)
    }
    return MessageNotificationsType.ALL
}

fun getMutedForGuild(client: Client, guildId: String): Boolean {
    return client.guildSettings[guildId]?.muted ?: false
}

fun getMutedForChannel(client: Client, channelId: String): Boolean {
    val channel = client.channels[channelId]
    val guildId = if (channel is GuildChannel) channel.guildId else "@me"
    var settings = client.guildSettings[guildId ?: "@me"]
    return settings?.channelOverrides?.get(channelId)?.muted ?: false
}

fun TextChannelBase.getMuted(): Boolean {
    if (this is GuildChannel) {
        var cursor: GuildChannel? = this
        var muted = false
        while (cursor != null && !muted) {
            muted = getMutedForChannel(client, this.id)
            cursor = cursor.parent
        }
        if (!muted) {
            muted = getMutedForGuild(client, this.guildId ?: "@me")
        }
        return muted
    } else if (this is Channel) {
        return getMutedForChannel(client, this.id)
    }
    return false
}

