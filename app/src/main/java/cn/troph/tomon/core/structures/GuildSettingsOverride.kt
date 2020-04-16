package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageNotificationsType
import com.google.gson.JsonObject

class GuildSettingsOverride(client: Client, data: JsonObject) : Base(client, data) {

    var channelId: String = ""
        private set
    var messageNotifications: MessageNotificationsType = MessageNotificationsType.DEFAULT
        private set
    var muted: Boolean = false
        private set

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("channel_id")) {
            channelId = data["channel_id"].asString
        }
        if (data.has("message_notifications")) {
            val value = data["message_notifications"].asInt
            messageNotifications =
                MessageNotificationsType.fromInt(value) ?: MessageNotificationsType.DEFAULT
        }
        if (data.has("muted")) {
            muted = data["muted"].asBoolean
        }
    }

}