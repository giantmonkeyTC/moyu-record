package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client

open class Base(val client: Client, private var data: Map<String, Any>) {
    init {
        patch(this.data)
    }

    val raw get() = this.data

    open fun patch(data: Map<String, Any>) {
        this.data += data;
    }

}