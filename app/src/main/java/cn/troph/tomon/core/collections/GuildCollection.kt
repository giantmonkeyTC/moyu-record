package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Guild

class GuildCollection(client: Client, m: Map<String, Guild>? = null) :
    BaseCollection<Guild>(client, m) {

    //TODO FETCH CREATE

    override fun instantiate(data: JsonData): Guild? {
        return Guild(client, data);
    }
}