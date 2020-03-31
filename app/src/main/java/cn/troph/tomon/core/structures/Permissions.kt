package cn.troph.tomon.core.structures

import cn.troph.tomon.core.utils.BitField

class Permissions(b: Any) : BitField(b) {
    companion object {
        const val createInstantInvite: Int = 1 shl 0
        const val kickMembers: Int = 1 shl 1
        const val banMembers: Int = 1 shl 2
        const val administrator: Int = 1 shl 3
        const val manageChannels: Int = 1 shl 4
        const val manageGuild: Int = 1 shl 5
        const val addReactions: Int = 1 shl 6
        const val viewAuditLog: Int = 1 shl 7
        const val prioritySpeaker: Int = 1 shl 8
        const val stream: Int = 1 shl 9
        const val viewChannel: Int = 1 shl 10
        const val sendMessages: Int = 1 shl 11
        const val sendTtsMessages: Int = 1 shl 12
        const val manageMessages: Int = 1 shl 13
        const val embedLinks: Int = 1 shl 14
        const val attachFiles: Int = 1 shl 15
        const val readMessageHistory: Int = 1 shl 16
        const val mentionEveryone: Int = 1 shl 17
        const val useExternalEmojis: Int = 1 shl 18

        const val connect: Int = 1 shl 20
        const val speak: Int = 1 shl 21
        const val muteMembers: Int = 1 shl 22
        const val deafenMembers: Int = 1 shl 23
        const val moveMembers: Int = 1 shl 24
        const val useVad: Int = 1 shl 25

        const val changeNickname: Int = 1 shl 26
        const val manageNicknames: Int = 1 shl 27
        const val manageRoles: Int = 1 shl 28
        const val manageWebhooks: Int = 1 shl 29
        const val manageEmojis: Int = 1 shl 30

        val flags = listOf(
            createInstantInvite,
            kickMembers,
            banMembers,
            administrator,
            manageChannels,
            manageGuild,
            addReactions,
            viewAuditLog,
            prioritySpeaker,
            stream,
            viewChannel,
            sendMessages,
            sendTtsMessages,
            manageMessages,
            embedLinks,
            attachFiles,
            readMessageHistory,
            mentionEveryone,
            useExternalEmojis,
            connect,
            speak,
            muteMembers,
            deafenMembers,
            moveMembers,
            useVad,
            changeNickname,
            manageNicknames,
            manageRoles,
            manageWebhooks,
            manageEmojis
        )

        const val defaultPermissions: Int = createInstantInvite or
                viewChannel or
                sendMessages or
                sendTtsMessages or
                embedLinks or
                attachFiles or
                readMessageHistory or
                mentionEveryone or
                useExternalEmojis or
                addReactions or
                connect or
                speak or
                changeNickname

        val all = flags.reduce { a, b -> a or b }
    }
}