package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.PermissionOverwriteType
import com.google.gson.JsonObject

class PermissionOverwrites(client: Client, data: JsonObject) : Base(client, data) {
    var id: String = ""
        private set
    var type: PermissionOverwriteType = PermissionOverwriteType.ROLE
        private set
    var allow = 0
        private set
    var deny = 0
        private set

    override fun patch(data: JsonObject) {
        super.patch(data)
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("type")) {
            val value = data["type"].asString
            type = PermissionOverwriteType.fromString(value) ?: PermissionOverwriteType.ROLE
        }
        if (data.has("allow")) {
            allow = data["allow"].asInt
        }
        if (data.has("deny")) {
            deny = data["deny"].asInt
        }
    }

}