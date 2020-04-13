package cn.troph.tomon.core

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

enum class MessageNotificationsType(val value: Int) {
    ALL(0),
    ONLY_MENTION(1),
    SUPPRESS(2),
    DEFAULT(3);

    companion object {
        private val map =
            MessageNotificationsType.values().associateBy(MessageNotificationsType::value)

        fun fromInt(type: Int) = map[type]
    }
}