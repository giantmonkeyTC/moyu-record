package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.utils.BitField

class Reaction(client: Client,data: JsonData):Base(client, data) {
    var count : Int =0
    var me : Boolean = false
    var emoji : Emoji? = null
    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.containsKey("count")) {
            count = data["count"] as Int
        }
        if (data.containsKey("me")) {
            me = data["me"] as Boolean
        }
        if (data.containsKey("emoji")) {
            emoji = Emoji(client,data["emoji"] as JsonData)
        }
    }

    override fun toString(): String {
        return "[CoreReaction ${emoji?.name}] { count: $count }"
    }
}