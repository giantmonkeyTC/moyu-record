package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.Context
import cn.troph.tomon.core.utils.GsonConflictStrategy
import cn.troph.tomon.core.utils.merge
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

open class Base(val client: Client, data: JsonObject) : ObservableOnSubscribe<Base> {

    private var emitter: ObservableEmitter<Base>? = null

    private val data: JsonObject = data

    val raw: JsonObject get() = data.deepCopy()

    override fun subscribe(emitter: ObservableEmitter<Base>?) {
        this.emitter = emitter
        emitter?.onNext(this)
    }

    fun update(data: JsonObject) {
        patch(data)
        emitter?.onNext(this)
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