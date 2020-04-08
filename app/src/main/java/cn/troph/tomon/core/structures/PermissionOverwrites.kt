package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.PermissionOverwriteType

class PermissionOverwrites(client: Client, data: JsonData) :
    Base(client, data) {
    var id: String = ""
    var type: PermissionOverwriteType = PermissionOverwriteType.ROLE
    var allow = 0
    var deny = 0

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("id")) {
            id = data["id"] as String;
        }
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