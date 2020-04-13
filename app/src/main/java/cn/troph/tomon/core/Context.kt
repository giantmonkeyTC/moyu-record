package cn.troph.tomon.core

import kotlinx.coroutines.newSingleThreadContext

object Context {
    val patch = newSingleThreadContext("core.patch")
}