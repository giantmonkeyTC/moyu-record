package cn.troph.tomon.ui.states

import io.reactivex.rxjava3.subjects.BehaviorSubject

class Variable<T>(defaultValue: T) {
    var value: T = defaultValue
        set(value) {
            field = value
            observable.onNext(value)
        }
    val observable: BehaviorSubject<T> = BehaviorSubject.createDefault(value)
}