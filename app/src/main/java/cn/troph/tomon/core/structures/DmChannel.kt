package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonArray
import cn.troph.tomon.core.JsonData

class DmChannel(
    client: Client,
    data: JsonData,
    private val mixin: TextChannelMixin = TextChannelMixin(client, data["id"] as String)
) : Channel(client, data), TextChannelBase by mixin {
    var recipientId: String = ""
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("recipients")) {
            val recipients = data["recipients"] as JsonArray
            if (recipients.isNotEmpty()) {
                val recipient = client.users.add(recipients[0])
                recipientId = recipient?.id ?: ""
            }
        }
    }

    val recipient get() = client.users.get(recipientId)
}