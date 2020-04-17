package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelDeleteEvent
import cn.troph.tomon.core.events.GuildChannelPositionEvent
import cn.troph.tomon.core.structures.CategoryChannel
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.utils.snowflake
import com.google.gson.Gson
import com.google.gson.JsonElement

class ChannelDeleteAction(client: Client) : Action<Channel>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): Channel? {
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["id"].asString]
        if (channel != null) {
            var needPosition = false
            if (channel is GuildChannel) {
                val children = if (channel is CategoryChannel) channel.children else null
                // 先计算兄弟节点按顺序大于自己位置的，调整位置
                if (channel.parent != null) {
                    val afters = channel.parent!!.children?.filter { child ->
                        if (child.position == channel.position)
                            child.id.snowflake > child.id.snowflake
                        else
                            child.position > channel.position
                    }
                    val offset = children?.size ?: 0 - 1
                    if (offset != 0) {
                        afters?.forEach { child ->
                            val json =
                                Gson().toJsonTree(
                                    mapOf(
                                        "position" to child.position + offset
                                    )
                                ).asJsonObject
                            child.update(json)
                        }
                        needPosition = needPosition or (afters?.size ?: 0 > 0)
                    }
                }
                // 将自己的孩子节点加入自己的父亲节点
                if (children != null) {
                    children.forEach { child ->
                        val json =
                            Gson().toJsonTree(
                                mapOf(
                                    "parent_id" to channel.parent?.id,
                                    "position" to child.position + channel.position
                                )
                            ).asJsonObject
                        child.update(json)
                    }
                    needPosition = needPosition or (children.size > 0)
                }
            }
            client.channels.remove(channel.id)
            client.eventBus.postEvent(ChannelDeleteEvent(channel))
            if (needPosition && channel is GuildChannel && channel.guild != null) {
                client.eventBus.postEvent(GuildChannelPositionEvent(channel.guild!!))
            }
        }
        return channel
    }
}