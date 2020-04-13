package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Base
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
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

    open fun add(data: JsonObject, identify: ((d: JsonObject) -> String)? = null): T? {
        val id = (if (identify != null) {
            identify(data)
        } else {
            data["id"].optString
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

    open fun instantiate(data: JsonObject): T? {
        return null
    }
}