package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject

class MessageReaction(client: Client, data: JsonObject, private val messageId: String) :
    Base(client, data) {

    data class EmojiData(val id: String? = null, val name: String? = null)

    var count: Int = 0
        private set
    var me: Boolean = false
        private set

    private var emojiData: EmojiData = EmojiData()

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("emoji")) {
            emojiData = parseEmojiData(data["emoji"].asJsonObject)
        }
        if (data.has("count")) {
            count = data["count"].asInt
        }
        if (data.has("me")) {
            me = data["me"].asBoolean
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    val id get() = getKey(emojiData)

    val name
        get(): String? {
            return if (isChar) {
                emojiData.name
            } else {
                emoji?.name
            }
        }

    val emoji get() = if (isChar) null else client.emojis[emojiData.id!!]

    val isChar get() = isChar(emojiData)

    companion object {

        fun parseEmojiData(data: JsonObject): EmojiData {
            val id = data["id"].optString
            val name = data["name"].optString ?: ""
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