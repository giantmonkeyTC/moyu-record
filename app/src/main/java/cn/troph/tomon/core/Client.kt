package cn.troph.tomon.core

import cn.troph.tomon.core.collections.ChannelCollection
import cn.troph.tomon.core.collections.GuildCollection
import cn.troph.tomon.core.collections.UserCollection

class Client {
    val users = UserCollection(this)
    val guilds = GuildCollection(this)
    val channels = ChannelCollection(this)
}