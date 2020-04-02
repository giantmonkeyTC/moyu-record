package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.collections.MessageCollection
import cn.troph.tomon.core.collections.PendMessageCollection

class TextChannel(client: Client, data: JsonData, guild: Guild) :
    GuildChannel(client, data, guild) {
    var topic = ""
    var lastMessageId = ""
    var ackMessageId = ""
    var syncAckMessageId = ""
    private var messageCollection = MessageCollection(this)
    private var pendMessageCollection = PendMessageCollection(this)

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.containsKey("topic")) {
            topic = data["topic"] as String;
        }
        if (data.containsKey("last_message_id")) {
            lastMessageId = data["last_message_id"] as String;
        }
        if (data.containsKey("ack_message_id")) {
            ackMessageId = data["ack_message_id"] as String;
            syncAckMessageId = data["ack_message_id"] as String;
        }
    }

    val unread get() : Boolean = false //TODO snowfalke.isgreaterthan

    val messages get() = messageCollection

    val pendingMessages get() = pendMessageCollection

    override fun toString(): String {
        return "[CoreTextChannel $id] { name: $name }"
    }

}