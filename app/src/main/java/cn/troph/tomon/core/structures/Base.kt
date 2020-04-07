package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe

open class Base(val client: Client, private var data: JsonData = mapOf()) :
    ObservableOnSubscribe<Any> {

    private var emitter: ObservableEmitter<Any>? = null

    init {
        patch(data)
    }

    val raw get() = data

    override fun subscribe(emitter: ObservableEmitter<Any>?) {
        this.emitter = emitter
    }

    fun update(data: JsonData) {
        patch(data)
        emitter?.onNext(this)
    }

    protected open fun patch(data: JsonData) {
        this.data += data
    }

}