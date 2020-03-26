package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class VoiceChannel(client: Client, data: JsonData, guild: Guild) :
    GuildChannel(client, data, guild) {


    override fun toString(): String {
        return "[CoreVoiceChannal $id] { name: $name }"
    }
}