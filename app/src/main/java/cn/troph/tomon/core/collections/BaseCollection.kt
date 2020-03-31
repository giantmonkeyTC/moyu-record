package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Base
import cn.troph.tomon.core.utils.Collection
import kotlin.reflect.KType
import kotlin.reflect.typeOf


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


    open fun resolve(idOrInstance: Any): Any? {
        if (this.values.contains(idOrInstance))
            return idOrInstance
        if (idOrInstance is String)
            return if (get(idOrInstance) == null) null else idOrInstance
        return null

    }


    //idOrInstance must be of the same type as T
    open fun resolveId(idOrInstance: Any, id: (value: T) -> String?): String? {

        return if (idOrInstance is String)
            idOrInstance
        else
            @Suppress("UNCHECKED_CAST")
            id(idOrInstance as T)
    }

    open fun instantiate(data: Map<String, Any>): T? {
        return null
    }
}