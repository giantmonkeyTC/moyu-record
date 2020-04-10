package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Role

class RoleCollection(client: Client, m: Map<String, Role>? = null, var guildId: String) :
    BaseCollection<Role>(client, m) {

    private var list: List<Role>? = null

    val guild get() = client.guilds.get(guildId)

    val everyone get() = get(guildId)

    //TODO FETCH CREATE

    fun list(): List<Role> {
        if (list == null) {
            val roles = values.toMutableList()
            roles.sortWith(Comparator { a, b ->
                a.comparePositionTo(b)
            })
            list = roles
        }
        return list!!
    }

    override fun instantiate(data: JsonData): Role? {
        return Role(client, data)
    }
}