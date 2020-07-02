package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.GuildPositionEvent
import cn.troph.tomon.core.utils.optJsonArray
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import org.json.JSONArray

class GuildPositionAction(client: Client) : Action<Unit>(client) {

    override fun handle(data: JsonElement?, vararg extras: Any?): Unit? {
        Logger.d(data?.toString())
        val posArray = data?.optJsonArray
        posArray?.let {
            val list = GsonBuilder().create().fromJson(it, Array<Position>::class.java)
                .toMutableList()
            client.eventBus.postEvent(GuildPositionEvent(list))
        }
        return Unit
    }
}

data class Position(
    @SerializedName("id") val id: String,
    @SerializedName("position") val position: Int
)