package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Base
import cn.troph.tomon.core.utils.Collection


open class BaseCollection<T : Base>(val client: Client, m: Map<String, T>? = null) :
    Collection<T>(m) {
    open fun add(data: Map<String, Any>, identify: ((d: Map<String, Any>) -> String)? = null): T? {
        val id = (if (identify != null) {
            identify(data)
        } else {
            data["id"] as String?
        })
            ?: return null
        val existing = get(id)
        if (existing != null) {
            existing.patch(data)
            return existing
        }
        val entry = instantiate(data);
        if (entry != null) {
            put(id, entry)
        }
        return entry
    }

    fun resolve(idOrInstance:Any): Any? {
        if(idOrInstance is Base)
        return idOrInstance
        if (idOrInstance is String)
            return if (get(idOrInstance)==null) null else idOrInstance
        return null

    }

    open fun instantiate(data: Map<String, Any>): T? {
        return null
    }
}