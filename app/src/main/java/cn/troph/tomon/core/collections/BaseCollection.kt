package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Base
import cn.troph.tomon.core.utils.Collection
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe

enum class EventType {
    ADD,
    REMOVE
}

data class Event<T>(val type: EventType, val obj: T)

open class BaseCollection<T : Base>(val client: Client, m: Map<String, T>? = null) :
    Collection<T>(m), ObservableOnSubscribe<Event<T>> {

    private var emitter: ObservableEmitter<Event<T>>? = null

    override fun subscribe(emitter: ObservableEmitter<Event<T>>?) {
        this.emitter = emitter
    }

    open fun add(data: JsonData, identify: ((d: JsonData) -> String)? = null): T? {
        val id = (if (identify != null) {
            identify(data)
        } else {
            data["id"] as String?
        })
            ?: return null
        val existing = get(id)
        if (existing != null) {
            existing.update(data)
            return existing
        }
        val entry = instantiate(data);
        if (entry != null) {
            put(id, entry)
            emitter?.onNext(Event(EventType.ADD, entry))
        }
        return entry
    }

    override fun remove(key: String): T? {
        val entry = super.remove(key)
        if (entry != null) {
            emitter?.onNext(Event(EventType.REMOVE, entry))
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

    open fun instantiate(data: JsonData): T? {
        return null
    }
}