package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Presence
import com.google.gson.JsonObject

class PresenceCollection(client: Client) :
    BaseCollection<Presence>(client) {

    override fun instantiate(data: JsonObject): Presence? {
        return Presence(client, data)
    }
}