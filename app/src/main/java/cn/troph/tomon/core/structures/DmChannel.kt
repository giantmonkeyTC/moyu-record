package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

class DmChannel(
    client: Client,
    data: JsonObject,
    private val mixin: TextChannelMixin = TextChannelMixin(client, data["id"].asString)
) : Channel(client, data), TextChannelBase by mixin {
    var recipientId: String = ""
        private set

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("recipients")) {
            val recipients = data["recipients"].asJsonArray
            if (recipients.size() > 0) {
                val recipient = client.users.add(recipients.get(0).asJsonObject)
                recipientId = recipient?.id ?: ""
            }
        }
    }

    val recipient get() = client.users.get(recipientId)
}