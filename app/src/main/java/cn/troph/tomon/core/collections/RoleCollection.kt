package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.Role
import com.google.gson.JsonObject

class RoleCollection(client: Client, var guild: Guild) :
    BaseCollection<Role>(client) {

    private var list: List<Role>? = null

    val everyone get() = get(guild.id)

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

    override fun instantiate(data: JsonObject): Role? {
        return Role(client, data)
    }
}