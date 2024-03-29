package cn.troph.tomon.core.structures

import android.text.BoringLayout
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.MessageNotificationsType
import cn.troph.tomon.core.collections.GuildChannelCollection
import cn.troph.tomon.core.collections.GuildEmojiCollection
import cn.troph.tomon.core.collections.GuildMemberCollection
import cn.troph.tomon.core.collections.RoleCollection
import cn.troph.tomon.core.utils.*
import com.google.gson.Gson
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import java.time.LocalDateTime
import java.time.ZonedDateTime

class Guild(client: Client, data: JsonObject) : Base(client, data), Comparable<Guild> {

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
    val channels: GuildChannelCollection = GuildChannelCollection(client, this)
    val members: GuildMemberCollection = GuildMemberCollection(client, this)
    val roles: RoleCollection = RoleCollection(client, this)
    val emojis: GuildEmojiCollection = GuildEmojiCollection(client, this)
    var unread = false
    var mention: Int = 0
    var isSelected = false
    var isVoiceChatting = false
    val voiceStates = mutableListOf<VoiceUpdate>()
    var backgroundUrl = ""

    init {
        patchSelf(data)
    }

    fun updateMention(): Boolean {
        val old = mention
        val cloned = channels.clone();
        mention = cloned.fold(0, { acc, element ->
            if (element is TextChannel) {
                acc + element.mention
            } else
                acc
        })
        return old != mention
    }

    fun updateUnread(): Boolean {
        unread = channels.clone().find { channel ->
            if (channel is TextChannel) {
                if (channel.unread)
                    return@find true
            }
            return@find false
        } != null
        return unread
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("background_url")) {
            backgroundUrl = data["background_url"].optString ?: ""
        }
        if (data.has("id")) {
            id = data["id"].optString ?: ""
        }
        if (data.has("name")) {
            name = data["name"].asString
        }
        if (data.has("icon")) {
            icon = data["icon"].optString
            if (icon?.isEmpty() != false) {
                icon = null
            }
        }
        if (data.has("icon_url")) {
            iconURL = data["icon_url"].optString
            if (iconURL?.isEmpty() != false) {
                iconURL = null
            }
        }
        if (data.has("position")) {
            position = data["position"].optInt ?: 0
        }
        if (data.has("joined_at")) {
            joinedAt = Converter.toDate(data["joined_at"].optString)
        }
        if (data.has("owner_id")) {
            ownerId = data["owner_id"].asString
        }
        if (data.has("system_channel_id")) {
            systemChannelId = data["system_channel_id"].optString
        }
        if (data.has("is_selected")) {
            isSelected = data["is_selected"].asBoolean
        }
        if (data.has("system_channel_flags")) {
            systemChannelFlags = data["system_channel_flags"].asInt
        }
        if (data.has("default_message_notifications")) {
            val value = data["default_message_notifications"].asInt
            defaultMessageNotifications =
                MessageNotificationsType.fromInt(value) ?: MessageNotificationsType.ONLY_MENTION
        }
        if (data.has("voice_states")) {
            voiceStates.clear()
            voiceStates.addAll(
                Gson().fromJson(
                    data.get("voice_states"),
                    Array<VoiceUpdate>::class.java
                )
            )
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    override fun compareTo(other: Guild): Int {
        val comparator = Comparator<Guild> { o1, o2 ->
            o1.position.compareTo(o2.position)
        }.then(Comparator { o1, o2 ->
            o1.joinedAt.compareTo(o2.joinedAt)
        }).then(Comparator { o1, o2 ->
            o1.id.snowflake.compareTo(o2.id.snowflake)
        })
        return comparator.compare(this, other)
    }
}