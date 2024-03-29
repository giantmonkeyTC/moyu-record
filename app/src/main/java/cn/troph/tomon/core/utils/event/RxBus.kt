package cn.troph.tomon.core.utils.event

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

class RxBus : EventBus {

    private val publishProcessor = PublishProcessor.create<Any>()

    override fun postEvent(event: Any) {
        publishProcessor.onNext(event)
    }

    override fun observeEvents(): Flowable<Any> {
        return publishProcessor.serialize().onBackpressureBuffer()
    }

    override fun observeEventsOnUi(): Flowable<Any> {
        return observeEvents().observeOn(AndroidSchedulers.mainThread()).onBackpressureBuffer()
    }
}