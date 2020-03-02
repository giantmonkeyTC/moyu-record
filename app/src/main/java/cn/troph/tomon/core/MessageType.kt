package cn.troph.tomon.core

enum class MessageType(val value: Int) {
    DEFAULT(0),
    RECIPIENT_ADD(1),
    RECIPIENT_REMOVE(2),
    GUILD_MEMBER_JOIN(3),
    CALL(4),
    CHANNEL_NAME_CHANGE(5),
    CHANNEL_ICON_CHANGE(6),
    CHANNEL_PINNED_MESSAGE(7),
    SYSTEM(8)
}