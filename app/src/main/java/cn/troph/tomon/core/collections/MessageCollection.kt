package cn.troph.tomon.core.collections

import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.Message

class MessageCollection(val channel: Channel) : BaseCollection<Message>(channel.client) {


    //TODO FETCH, CREATE, ETC

    override fun instantiate(data: JsonData): Message? {
        return Message(client, data, channel)
    }

}