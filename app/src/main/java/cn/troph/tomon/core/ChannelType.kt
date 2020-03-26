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

enum class PermissionOverwriteType(){
    ROLE,
    MEMBER
}