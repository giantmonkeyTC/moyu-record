package cn.troph.tomon.core

enum class ChannelType(val value: Int) {
    TEXT(0),
    VOICE(1),
    DM(2),
    GROUP(3),
    CATEGORY(4)
}