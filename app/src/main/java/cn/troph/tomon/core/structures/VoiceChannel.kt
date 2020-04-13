package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import com.google.gson.JsonObject

class VoiceChannel(client: Client, data: JsonObject) :
    GuildChannel(client, data) {

}