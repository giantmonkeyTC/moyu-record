package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client

abstract class Action<T>(protected val client: Client) {
    open fun handle(data: Any): T? {
        return null
    }
}