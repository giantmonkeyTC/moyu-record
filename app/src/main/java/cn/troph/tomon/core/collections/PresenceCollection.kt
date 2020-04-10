package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Presence

class PresenceCollection(client: Client) :
    BaseCollection<Presence>(client) {

    override fun instantiate(data: JsonData): Presence? {
        return Presence(client, data)
    }
}