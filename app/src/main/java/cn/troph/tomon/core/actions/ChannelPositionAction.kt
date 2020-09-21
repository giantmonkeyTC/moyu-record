package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.ChannelPositionEvent
import cn.troph.tomon.core.events.GuildPositionEvent
import cn.troph.tomon.core.utils.optJsonArray
import cn.troph.tomon.core.utils.optString
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

class ChannelPositionAction(client: Client) : Action<Unit>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): Unit? {
        val obj = data?.asJsonObject
        if (obj == null) {
            return null
        }

        val positions = obj.get("positions").asJsonArray
        val positionList = mutableListOf<Position>()
        for ( pos in positions) {
            val posObj = pos.asJsonObject
            var parent_id:String? = "default"
            if (posObj.has("parent_id")) {
                parent_id = posObj.get("parent_id").optString
            }
            val item = Position(
                pos.asJsonObject.get("id").optString?:"",
                posObj.get("position").asInt,
                parent_id
            )
            positionList.add(item)
        }

        val guildId = obj.get("guild_id").optString ?: ""
        client.eventBus.postEvent(ChannelPositionEvent(guildId, positionList))
        return Unit
    }
}