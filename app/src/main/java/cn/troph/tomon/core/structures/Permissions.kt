package cn.troph.tomon.core.structures

class Permissions {

    var value: Long = 0

    constructor(b: Any?) {
        value = resolve(b)
    }

    fun has(bit: Long): Boolean {
        return (value and bit) == bit
    }

    fun plus(b: Any?): Permissions {
        return Permissions(value or resolve(b))
    }

    fun minus(b: Any?): Permissions {
        return Permissions(value and resolve(b).inv())
    }

    override fun equals(b: Any?): Boolean {
        return if (b == null) false else value == resolve(b)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {

        fun resolve(b: Any?): Long {
            return when (b) {
                null -> 0
                is Int -> b.toLong()
                is Long -> b
                is Permissions -> b.value
                is Iterable<*> -> b.map { p -> resolve(p) }.fold(0L, { acc, p -> acc or p })
                else -> throw IllegalArgumentException("unsolved input")
            }
        }

        const val CREATE_INSTANT_INVITE: Long = 1 shl 0
        const val KICK_MEMBERS: Long = 1 shl 1
        const val BAN_MEMBERS: Long = 1 shl 2
        const val ADMINISTRATOR: Long = 1 shl 3
        const val MANAGE_CHANNELS: Long = 1 shl 4
        const val MANAGE_GUILD: Long = 1 shl 5
        const val ADD_REACTIONS: Long = 1 shl 6
        const val VIEW_AUDIT_LOG: Long = 1 shl 7
        const val PRIORITY_SPEAKER: Long = 1 shl 8
        const val STREAM: Long = 1 shl 9
        const val VIEW_CHANNEL: Long = 1 shl 10
        const val SEND_MESSAGES: Long = 1 shl 11
        const val SEND_TTS_MESSAGES: Long = 1 shl 12
        const val MANAGE_MESSAGES: Long = 1 shl 13
        const val EMBED_LINKS: Long = 1 shl 14
        const val ATTACH_FILES: Long = 1 shl 15
        const val READ_MESSAGE_HISTORY: Long = 1 shl 16
        const val MENTION_EVERYONE: Long = 1 shl 17
        const val USE_EXTERNAL_EMOJIS: Long = 1 shl 18

        const val CONNECT: Long = 1 shl 20
        const val SPEAK: Long = 1 shl 21
        const val MUTE_MEMBERS: Long = 1 shl 22
        const val DEAFEN_MEMBERS: Long = 1 shl 23
        const val MOVE_MEMBERS: Long = 1 shl 24
        const val USE_VAD: Long = 1 shl 25

        const val CHANGE_NICKNAME: Long = 1 shl 26
        const val MANAGE_NICKNAMES: Long = 1 shl 27
        const val MANAGE_ROLES: Long = 1 shl 28
        const val MANAGE_WEBHOOKS: Long = 1 shl 29
        const val MANAGE_EMOJIS: Long = 1 shl 30

        fun all(): Permissions {
            return Permissions(0xffffffff)
        }

        fun default(): Permissions {
            return Permissions(
                CREATE_INSTANT_INVITE or
                        VIEW_CHANNEL or
                        SEND_MESSAGES or
                        SEND_TTS_MESSAGES or
                        EMBED_LINKS or
                        ATTACH_FILES or
                        READ_MESSAGE_HISTORY or
                        MENTION_EVERYONE or
                        USE_EXTERNAL_EMOJIS or
                        ADD_REACTIONS or
                        CONNECT or
                        SPEAK or
                        CHANGE_NICKNAME
            )
        }

    }

}

