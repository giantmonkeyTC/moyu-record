package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Role

class RoleCollection(client: Client, m: Map<String, Role>? = null, var guildId: String) :
    BaseCollection<Role>(client, m) {

    val guild get() = client.guilds.get(guildId)

    val everyone get() = get(guildId)

    override fun instantiate(data: Map<String, Any>): Role? {
        return Role(client, data)
    }
}