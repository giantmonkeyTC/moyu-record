package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageNotificationsType
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject

class GuildSettings(client: Client, data: JsonObject) : Base(client, data) {

    var guildId: String? = null
        private set
    var messageNotifications: MessageNotificationsType = MessageNotificationsType.DEFAULT
        private set
    var muted: Boolean = false
        private set
    var suppressEveryone: Boolean = false
        private set
    var channelOverrides: Collection<GuildSettingsOverride> = Collection()
        private set

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("guild_id")) {
            guildId = data["guild_id"].optString
        }
        if (data.has("message_notifications")) {
            val value = data["message_notifications"].asInt
            messageNotifications =
                MessageNotificationsType.fromInt(value) ?: MessageNotificationsType.DEFAULT
        }
        if (data.has("muted")) {
            muted = data["muted"].asBoolean
        }
        if (data.has("suppress_everyone")) {
            suppressEveryone = data["suppress_everyone"].asBoolean
        }
        if (data.has("channel_overrides")) {
            channelOverrides = Collection()
            val array = data["channel_overrides"].asJsonArray
            array.forEach { obj ->
                val o = obj.asJsonObject
                channelOverrides[o["channel_id"].asString] = GuildSettingsOverride(client, o)
            }
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }
}