package cn.troph.tomon.core

typealias JsonData = Map<String, Any?>

typealias JsonArray = List<JsonData>

enum class ChannelType(val value: Int) {
    TEXT(0),
    VOICE(1),
    DM(2),
    GROUP(3),
    CATEGORY(4);

    companion object {
        private val map = ChannelType.values().associateBy(ChannelType::value)
        fun fromInt(type: Int) = map[type]
    }
}

enum class PermissionOverwriteType(val value: String) {
    ROLE("role"),
    MEMBER("member");

    companion object {
        private val map =
            PermissionOverwriteType.values().associateBy(PermissionOverwriteType::value)

        fun fromString(type: String) = map[type]
    }
}

enum class MessageType(val value: Int) {
    DEFAULT(0),
    RECIPIENT_ADD(1),
    RECIPIENT_REMOVE(2),
    GUILD_MEMBER_JOIN(3),
    CALL(4),
    CHANNEL_NAME_CHANGE(5),
    CHANNEL_ICON_CHANGE(6),
    CHANNEL_PINNED_MESSAGE(7),
    SYSTEM(8);

    companion object {
        private val map = MessageType.values().associateBy(MessageType::value)
        fun fromInt(type: Int) = map[type]
    }
}

enum class Permission(val value: Int) {
    CREATE_INSTANT_INVITE(1 shl 0),
    KICK_MEMBERS(1 shl 1),
    BAN_MEMBERS(1 shl 2),
    ADMINISTRATOR(1 shl 3),
    MANAGE_CHANNELS(1 shl 4),
    MANAGE_GUILD(1 shl 5),
    ADD_REACTIONS(1 shl 6),
    VIEW_AUDIT_LOG(1 shl 7),
    PRIORITY_SPEAKER(1 shl 8),
    STREAM(1 shl 9),
    VIEW_CHANNEL(1 shl 10),
    SEND_MESSAGES(1 shl 11),
    SEND_TTS_MESSAGES(1 shl 12),
    MANAGE_MESSAGES(1 shl 13),
    EMBED_LINKS(1 shl 14),
    ATTACH_FILES(1 shl 15),
    READ_MESSAGE_HISTORY(1 shl 16),
    MENTION_EVERYONE(1 shl 17),
    USE_EXTERNAL_EMOJIS(1 shl 18),

    CONNECT(1 shl 20),
    SPEAK(1 shl 21),
    MUTE_MEMBERS(1 shl 22),
    DEAFEN_MEMBERS(1 shl 23),
    MOVE_MEMBERS(1 shl 24),
    USE_VAD(1 shl 25),

    CHANGE_NICKNAME(1 shl 26),
    MANAGE_NICKNAMES(1 shl 27),
    MANAGE_ROLES(1 shl 28),
    MANAGE_WEBHOOKS(1 shl 29),
    MANAGE_EMOJIS(1 shl 30),
}