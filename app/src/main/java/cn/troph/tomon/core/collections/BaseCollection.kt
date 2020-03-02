package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Base
import cn.troph.tomon.core.utils.Collection


open class BaseCollection<T : Base>(val client: Client, m: Map<String, T>? = null) : Collection<T>(m) {
    open fun add(data: Map<String, Any>, identify: ((d: Map<String, Any>) -> String)?): T? {
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

    open fun instantiate(data: Map<String, Any>): T? {
        return null
    }
}