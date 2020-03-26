package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.PermissionOverwriteType

class PermissionOverwrites(client: Client, data: JsonData, val guildChannel: GuildChannel) :
    Base(client, data) {
    var id: String = ""
    var type: PermissionOverwriteType? = null
    var allow = 0
    var deny = 0

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.containsKey("id")) {
            id = data["id"] as String;
        }
        if (data.containsKey("type")) {
            type =
                if ((data["type"] as String) == "role")
                    PermissionOverwriteType.ROLE
                else
                    PermissionOverwriteType.MEMBER;
        }
        if (data.containsKey("allow")) {
            var raw = data["allow"];
            if (raw is Int) {
                allow = raw;
            } else if (raw is String) {
                allow = raw.toInt();
            }
        }
        if (data.containsKey("deny")) {
            var raw = data["deny"];
            if (raw is Int) {
                deny = raw;
            } else if (raw is String) {
                deny = raw.toInt();
            }
        }
    }

    override fun toString(): String {
        return "[CorePermissionOverwrites $id] { type: $type, allow: $allow, deny: $deny }"
    }
}