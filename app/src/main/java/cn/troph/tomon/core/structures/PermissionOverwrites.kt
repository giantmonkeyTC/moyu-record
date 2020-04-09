package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.PermissionOverwriteType

class PermissionOverwrites(client: Client, data: JsonData) :
    Base(client, data) {
    val id: String = data["id"] as String
    var type: PermissionOverwriteType = PermissionOverwriteType.ROLE
        private set
    var allow = 0
        private set
    var deny = 0
        private set

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("type")) {
            val value = data["type"] as String
            type = PermissionOverwriteType.fromString(value) ?: PermissionOverwriteType.ROLE
        }
        if (data.contains("allow")) {
            var raw = data["allow"];
            allow = when (raw) {
                is Int -> raw
                is String -> raw.toInt()
                else -> 0
            }
        }
        if (data.contains("deny")) {
            var raw = data["deny"];
            deny = when (raw) {
                is Int -> raw
                is String -> raw.toInt()
                else -> 0
            }
        }
    }

}