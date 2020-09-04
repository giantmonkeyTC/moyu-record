package cn.troph.tomon.core.network.socket.handlers

import android.util.Log
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.Handler
import cn.troph.tomon.core.structures.StampPack
import cn.troph.tomon.core.utils.Assets
import cn.troph.tomon.core.utils.optString
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

val handleIdentity: Handler = { client: Client, packet: JsonElement ->
    val data = packet.asJsonObject["d"].asJsonObject
    // data pre-processing
    data["guilds"].asJsonArray.forEach { ele ->
        val guild = ele.asJsonObject
        if (guild["system_channel_id"].optString == "0") {
            guild.add("system_channel_id", JsonNull.INSTANCE)
        }
    }
    data["dm_channels"].asJsonArray.forEach { ele ->
        val channel = ele.asJsonObject
        val guildId = channel["guild_id"]?.optString
        if (guildId == "0") {
            channel.addProperty("guild_id", "@me")
        }
    }
    data["guild_settings"].asJsonArray.forEach { ele ->
        val settings = ele.asJsonObject
        if (settings["guild_id"].optString == "0") {
            settings.add("guild_id", JsonNull.INSTANCE)
        }
    }
    client.actions.guildFetch(data["guilds"].asJsonArray, true)
    client.actions.channelFetch(data["dm_channels"].asJsonArray, true)
    data["guilds"].asJsonArray.forEach {
        val guild = it.asJsonObject
        client.actions.channelFetch(guild["channels"].asJsonArray, true, guild["id"].asString)
        client.actions.roleFetch(guild["roles"].asJsonArray, true, guild["id"].asString)
        client.actions.emojiFetch(guild["emojis"].asJsonArray, true, guild["id"].asString)
        client.actions.guildMemberFetch(guild["members"].asJsonArray, true, guild["id"].asString)
        client.actions.presenceFetch(guild["presences"].asJsonArray, guild["id"].asString)
    }


    data["guild_settings"].asJsonArray.forEach { e ->
        client.actions.guildSettingsUpdate(e.asJsonObject)
    }
    client.stamps.clear()
    data["stamp_packs"].asJsonArray.forEach { s ->
        val stampPack = s.asJsonObject
        client.stamps.add(Gson().fromJson(stampPack, StampPack::class.java))
    }
    Client.global.rest.guildEmojiService.fetchStampPack(
        Assets.defaultStampPackId,
        Client.global.auth
    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe({ Client.global.stamps.add(Gson().fromJson(it, StampPack::class.java)) }, {
        })
}