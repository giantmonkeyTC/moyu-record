package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import java.util.*

class GuildMember (client: Client,data: JsonData,val guild:Guild): Base(client, data){
    var nick =""
    var joinedAt: Date? = null

}