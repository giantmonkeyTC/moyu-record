package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Base
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.optString
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.subjects.PublishSubject

enum class EventType {
    SET,
    REMOVE,
    CLEAR,
    INIT
}

typealias CollectionIdentify = (d: JsonObject) -> String?

data class Event<T>(val type: EventType, val obj: T? = null)

open class BaseCollection<T : Base>(val client: Client) :
    Collection<T>(null) {

    var observable: PublishSubject<Event<T>> = PublishSubject.create()

    open fun add(data: JsonObject, identify: CollectionIdentify? = null): T? {
        val id = (if (identify != null) {
            identify(data)
        } else {
            data["id"].optString
        })
        if (id != null) {
            val existing = get(id)
            if (existing != null) {
                existing.update(data)
                return existing
            }
        }
        val entry = instantiate(data);
        if (id != null && entry != null) {
            put(id, entry)
        }
        return entry
    }

    override operator fun set(key: String, value: T): T? {
        val entry = super.set(key, value)
        observable.onNext(Event(EventType.SET, entry))
        return entry
    }

    override fun remove(key: String): T? {
        val entry = super.remove(key)
        if (entry != null) {
            observable.onNext(Event(EventType.REMOVE, entry))
        }
        return entry
    }

    override fun clear() {
        super.clear()
        observable.onNext(Event(EventType.CLEAR))
    }

    open fun instantiate(data: JsonObject): T? {
        return null
    }
}