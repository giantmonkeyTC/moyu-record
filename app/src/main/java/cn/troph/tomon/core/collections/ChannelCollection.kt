package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild

class ChannelCollection(client: Client, m: Map<String, Guild>? = null) :
    BaseCollection<Guild>(client, m) {
}