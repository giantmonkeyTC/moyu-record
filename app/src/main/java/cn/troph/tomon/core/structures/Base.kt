package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.GsonConflictStrategy
import cn.troph.tomon.core.utils.merge
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

open class Base(val client: Client, data: JsonObject) {

    val observable: PublishSubject<Base> = PublishSubject.create()

    private val data: JsonObject = data

    val raw: JsonObject get() = data.deepCopy()

    fun update(data: JsonObject) {
        patch(data)
        observable.onNext(this)
    }

    fun update(data: Map<String, Any?>) {
        val obj = Gson().toJsonTree(data)
        update(obj.asJsonObject)
    }

    fun update(field: String, value: Any?) {
        update(mapOf(field to value))
    }

    open fun patch(data: JsonObject) {
        this.data.merge(data, GsonConflictStrategy.PREFER_SECOND_OBJ)
    }

}