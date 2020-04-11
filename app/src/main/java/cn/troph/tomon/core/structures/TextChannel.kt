package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class TextChannel(
    client: Client,
    data: JsonData,
    private val mixin: TextChannelMixin = TextChannelMixin(client, data["id"] as String)
) : GuildChannel(client, data), TextChannelBase by mixin {

    var topic: String? = null
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.containsKey("topic")) {
            topic = data["topic"] as String;
        }
        mixin.patch(data)
    }

}