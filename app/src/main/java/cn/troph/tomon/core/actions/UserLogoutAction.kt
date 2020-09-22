package cn.troph.tomon.core.actions

import androidx.core.content.edit
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.UserLogoutEvent
import com.google.gson.JsonElement

class UserLogoutAction(client: Client) : Action<Unit>(client) {
    override fun handle(data: JsonElement?, vararg extras: Any?): Unit? {
        client.users.clear()
        client.guilds.clear()
        client.guilds.list.toList().clear()
        client.channels.clear()
        client.dmChannels.clear()
        client.me.clear()
        client.stamps.clear()
        client.preferences.edit {
            remove("token").commit()
        }
        client.socket.close()
        client.eventBus.postEvent(UserLogoutEvent())
        return null
    }
}