package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import com.google.gson.JsonElement

abstract class Action<T>(protected val client: Client) {
    open fun handle(data: JsonElement?, vararg extras: Any?): T? {
        return null
    }
}