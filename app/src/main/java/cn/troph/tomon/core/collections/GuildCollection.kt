package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild

class GuildCollection(client: Client, m: Map<String, Guild>? = null) :
    BaseCollection<Guild>(client, m) {

    override fun instantiate(data: Map<String, Any>): Guild? {
        return Guild(client, data);
    }
}