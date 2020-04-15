package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.Context
import cn.troph.tomon.core.utils.GsonConflictStrategy
import cn.troph.tomon.core.utils.merge
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

open class Base(val client: Client, data: JsonObject) : ObservableOnSubscribe<Any> {

    private var emitter: ObservableEmitter<Any>? = null

    private val data: JsonObject = JsonObject()

    val raw: JsonObject get() = data.deepCopy()

    init {
        init(data)
        patch(data)
    }

    override fun subscribe(emitter: ObservableEmitter<Any>?) {
        this.emitter = emitter
    }

    fun update(data: JsonObject) {
        runBlocking {
            withContext(Context.patch) {
                patch(data)
            }
            emitter?.onNext(this)
        }
    }

    open fun init(data: JsonObject) {

    }

    open fun patch(data: JsonObject) {
        this.data.merge(data, GsonConflictStrategy.PREFER_SECOND_OBJ)
    }

}