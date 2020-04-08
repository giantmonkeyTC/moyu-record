package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.MessageNotificationsType
import cn.troph.tomon.core.collections.GuildChannelCollection
import cn.troph.tomon.core.collections.GuildMemberCollection
import cn.troph.tomon.core.collections.RoleCollection
import java.time.LocalDateTime

class Guild(client: Client, data: JsonData) : Base(client, data) {
    var id: String = ""
        private set
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

    val channels: GuildChannelCollection = GuildChannelCollection(this)
    val members: GuildMemberCollection = GuildMemberCollection(this)
    val roles: RoleCollection = RoleCollection(this.client, guildId = id)

    //TODO LEAVE, UPDATE, ETC.

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("id")) {
            id = data["id"] as String
        }
        if (data.contains("name")) {
            name = data["name"] as String
        }
        if (data.contains("icon")) {
            icon = data["icon"] as String?
        }
        if (data.contains("icon_url")) {
            iconURL = data["iconURL"] as String?
        }
        if (data.contains("position")) {
            position = data["position"] as Int
        }
        if (data.contains("joined_at")) {
            val date = data["joinedAt"] as String
            joinedAt = LocalDateTime.parse(date)
        }
        if (data.contains("owner_id")) {
            ownerId = data["ownerId"] as String
        }
        if (data.contains("system_channel_id")) {
            systemChannelId = data["system_channel_id"] as String?
        }
        if (data.contains("system_channel_flags")) {
            systemChannelFlags = data["system_channel_flags"] as Int
        }
        if (data.contains("default_message_notifications")) {
            val value = data["default_message_notifications"] as Int
            defaultMessageNotifications = MessageNotificationsType.fromInt(value)
        }
    }
}