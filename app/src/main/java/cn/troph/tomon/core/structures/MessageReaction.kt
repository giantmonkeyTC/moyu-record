package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class MessageReaction(client: Client, data: JsonData, private val messageId: String) :
    Base(client, data) {

    data class EmojiData(val id: String? = null, val name: String? = null)

    var count: Int = 0
        private set
    var me: Boolean = false
        private set
    private var emojiData: EmojiData = EmojiData()

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("emoji")) {
            emojiData = parseEmojiData(data["emoji"] as JsonData)
        }
        if (data.contains("count")) {
            count = data["count"] as Int
        }
        if (data.contains("me")) {
            me = data["me"] as Boolean
        }
    }

    val id get() = getKey(emojiData)

    val name get(): String? {
        return if (isChar) {
            emojiData.name
        } else {
            emoji?.name
        }
    }

    val emoji get() = if (isChar) null else client.emojis.get(emojiData.id!!)

    val isChar get() = isChar(emojiData)

    companion object {

        fun parseEmojiData(data: JsonData): EmojiData {
            val id = data["id"] as? String
            val name = data["name"] as? String ?: ""
            return EmojiData(id, name)
        }

        private fun isChar(data: EmojiData): Boolean {
            return data.id == null
        }

        fun getKey(data: EmojiData): String {
            return if (isChar(data)) {
                "_${data.name}"
            } else {
                data.id ?: ""
            }
        }
    }

}