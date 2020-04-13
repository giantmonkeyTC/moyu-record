package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

class TextChannel(
    client: Client,
    data: JsonObject,
    private val mixin: TextChannelMixin = TextChannelMixin(client, data["id"].asString)
) : GuildChannel(client, data), TextChannelBase by mixin {

    var topic: String? = null
        private set

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("topic")) {
            topic = data["topic"].asString;
        }
        mixin.patch(data)
    }

}