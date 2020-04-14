package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageNotificationsType
import cn.troph.tomon.core.collections.GuildChannelCollection
import cn.troph.tomon.core.collections.GuildEmojiCollection
import cn.troph.tomon.core.collections.GuildMemberCollection
import cn.troph.tomon.core.collections.RoleCollection
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Guild(client: Client, data: JsonObject) : Base(client, data) {
    val id: String = data["id"].asString
    var name: String = ""
        private set
    var icon: String? = null
        private set
    var iconURL: String? = null
        private set
    var position: Int = 0
        private set
    var joinedAt: LocalDateTime = LocalDateTime.now()
        private set
    var ownerId: String = ""
        private set
    var systemChannelId: String? = null
        private set
    var systemChannelFlags: Int = 0
        private set
    var defaultMessageNotifications: MessageNotificationsType =
        MessageNotificationsType.ONLY_MENTION
        private set

    val channels: GuildChannelCollection = GuildChannelCollection(client, guildId = id)
    val members: GuildMemberCollection = GuildMemberCollection(client, guildId = id)
    val roles: RoleCollection = RoleCollection(client, guildId = id)
    val emojis: GuildEmojiCollection = GuildEmojiCollection(client, guildId = id)

    //TODO LEAVE, UPDATE, ETC.

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("name")) {
            name = data["name"].asString
        }
        if (data.has("icon")) {
            icon = data["icon"].optString
        }
        if (data.has("icon_url")) {
            iconURL = data["icon_url"].optString
        }
        if (data.has("position")) {
            position = data["position"].asInt
        }
        if (data.has("joined_at")) {
            joinedAt = LocalDateTime.parse(data["joined_at"].asString, DateTimeFormatter.ISO_DATE_TIME)
        }
        if (data.has("owner_id")) {
            ownerId = data["owner_id"].asString
        }
        if (data.has("system_channel_id")) {
            systemChannelId = data["system_channel_id"].optString
        }
        if (data.has("system_channel_flags")) {
            systemChannelFlags = data["system_channel_flags"].asInt
        }
        if (data.has("default_message_notifications")) {
            val value = data["default_message_notifications"].asInt
            defaultMessageNotifications =
                MessageNotificationsType.fromInt(value) ?: MessageNotificationsType.ONLY_MENTION
        }
    }
}