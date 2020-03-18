package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import java.time.LocalDateTime

class Guild(client: Client, data: JsonData) : Base(client, data) {
    var id: String = ""
    var name: String = ""
    var icon: String? = null
    var iconURL: String? = null
    var position: Int = 0
    var joinedAt: LocalDateTime = LocalDateTime.now()
    var ownerId: String = ""
    var systemChannelId: String? = null
    var systemChannelFlags: Int = 0

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
        if (data.contains("iconURL")) {
            iconURL = data["iconURL"] as String?
        }
        if (data.contains("position")) {
            position = data["position"] as Int
        }
        if (data.contains("joinedAt")) {
            val date = data["joinedAt"] as String
            joinedAt = LocalDateTime.parse(date)
        }
        if (data.contains("ownerId")) {
            ownerId = data["ownerId"] as String
        }
        if (data.contains("system_channel_id")) {
            systemChannelId = data["system_channel_id"] as String?
        }
        if (data.contains("system_channel_flags")) {
            systemChannelFlags = data["system_channel_flags"] as Int
        }
    }
}