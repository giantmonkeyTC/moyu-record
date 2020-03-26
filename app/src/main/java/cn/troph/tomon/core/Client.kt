package cn.troph.tomon.core

import cn.troph.tomon.core.actions.ActionManager
import cn.troph.tomon.core.collections.ChannelCollection
import cn.troph.tomon.core.collections.GuildCollection
import cn.troph.tomon.core.collections.UserCollection
import cn.troph.tomon.core.network.Restful
import cn.troph.tomon.core.network.socket.Socket
import cn.troph.tomon.core.structures.Me

class Client {

    val rest = Restful()
    val actions = ActionManager(this)
    val socket = Socket(this)

    val me = Me(this)
    val users = UserCollection(this)
    val guilds = GuildCollection(this)
    val channels = ChannelCollection(this)

    fun initialize() {
        socket.open()
    }
}