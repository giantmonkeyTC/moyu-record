package cn.troph.tomon.core.utils.event

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.ofType

interface EventBus {

    fun postEvent(event: Any)

    fun observeEvents(): Flowable<Any>

    fun observeEventsOnUi(): Flowable<Any>
}

inline fun <reified T : Any> EventBus.observeEvent(): Flowable<T> {
    return observeEvents().onBackpressureBuffer().ofType()
}

inline fun <reified T : Any> EventBus.observeEventOnUi(): Flowable<T> {
    return observeEventsOnUi().onBackpressureBuffer().ofType()
}