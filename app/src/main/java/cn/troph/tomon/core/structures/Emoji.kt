package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.utils.BitField

class Emoji(client: Client, data: JsonData) : Base(client, data) {
    var name: String = ""
    var id : String = ""
    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.containsKey("name")) {
            name = data["name"] as String
        } else if (data.containsKey("id")) {
            id = data["id"] as String
        }
    }

    override fun toString(): String {
        return if (id==null) "[CoreEmoji $name] { emojiName: $name }" else "[CoreEmoji $id] { emojiID: $id }"

    }
    val assest get() = "https://troph-1255393139.file.myqcloud.com/emojis/${id}.png"
}