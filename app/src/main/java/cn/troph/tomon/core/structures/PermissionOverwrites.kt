package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.PermissionOverwriteType
import com.google.gson.JsonObject

class PermissionOverwrites(client: Client, data: JsonObject) : Base(client, data) {
    var id: String = ""
        private set
    var type: PermissionOverwriteType = PermissionOverwriteType.ROLE
        private set
    var allow: Long = 0
        private set
    var deny: Long = 0
        private set

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("type")) {
            val value = data["type"].asString
            type = PermissionOverwriteType.fromString(value) ?: PermissionOverwriteType.ROLE
        }
        if (data.has("allow")) {
            allow = data["allow"].asLong
        }
        if (data.has("deny")) {
            deny = data["deny"].asLong
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

    fun denyPermission(bit: Long): Boolean {
        return (deny and bit) == bit
    }

}