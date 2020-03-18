package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client

typealias JsonData = Map<String, Any>

open class Base(val client: Client, private var data: JsonData = mapOf()) {
    init {
        patch(this.data)
    }

    val raw get() = this.data

    open fun patch(data: JsonData) {
        if (data != null) {
            this.data += data;
        }
    }

}