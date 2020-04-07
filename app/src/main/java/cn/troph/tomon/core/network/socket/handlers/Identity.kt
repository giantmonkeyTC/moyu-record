package cn.troph.tomon.core.network.socket.handlers

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.network.socket.Handler

val handleIdentity: Handler = { client: Client, packet: JsonData ->
    println("identity")
    println(packet)
}