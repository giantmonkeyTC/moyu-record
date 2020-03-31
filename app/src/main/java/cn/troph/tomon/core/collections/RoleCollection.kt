package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
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
            roles.sortWith(Comparator { a: Role, b: Role ->
                var compare: Int =
                    (if (a.isEveryone) 1 else 0).compareTo(if (b.isEveryone) 1 else 0)
                if(compare == 0)
                    compare = b.position.compareTo(a.position)
                if(compare == 0)
                    compare = a.id.compareTo(b.id)
                compare
            })
            list = roles
        }
        return list as List<Role>
    }


    override fun instantiate(data: Map<String, Any>): Role? {
        return Role(client, data)
    }
}