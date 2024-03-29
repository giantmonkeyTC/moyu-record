package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ActionManager(val client: Client) {

    fun userLogin(data: JsonObject): User? = UserLoginAction(client).handle(data)
    fun userLogout(): Unit? = UserLogoutAction(client).handle(null)
    fun userUpdate(data: JsonObject): User? = UserUpdateAction(client).handle(data)
    fun userRegister(data: JsonObject): User? = UserRegisterAction(client).handle(data)

    fun guildFetch(data: JsonArray, isSync: Boolean = true): List<Guild>? =
        GuildFetchAction(client).handle(data, isSync)

    fun guildCreate(data: JsonObject): Guild? = GuildCreateAction(client).handle(data)
    fun guildDelete(data: JsonObject): Guild? = GuildDeleteAction(client).handle(data)
    fun guildUpdate(data: JsonObject): Guild? = GuildUpdateAction(client).handle(data)
    fun guildPosition(data: JsonObject): Unit? = GuildPositionAction(client).handle(data)

    fun channelFetch(
        data: JsonElement,
        isSync: Boolean = true,
        guildId: String? = null
    ): List<Channel>? = ChannelFetchAction(client).handle(data, isSync, guildId)

    fun channelCreate(data: JsonObject): Channel? = ChannelCreateAction(client).handle(data)
    fun channelDelete(data: JsonObject): Channel? = ChannelDeleteAction(client).handle(data)
    fun channelUpdate(data: JsonObject): Channel? = ChannelUpdateAction(client).handle(data)
    fun channelPosition(data: JsonObject): Unit? = ChannelPositionAction(client).handle(data)
    fun channelAck(acks:JsonArray): Unit? =
        ChannelAckAction(client).handle(acks)

    fun roleFetch(
        data: JsonElement,
        isSync: Boolean = true,
        guildId: String? = null
    ): List<Role>? = RoleFetchAction(client).handle(data, isSync, guildId)

    fun emojiFetch(
        data: JsonElement,
        isSync: Boolean = true,
        guildId: String? = null
    ): List<GuildEmoji>? = EmojiFetchAction(client).handle(data, isSync, guildId)

    fun guildMemberFetch(
        data: JsonElement,
        isSync: Boolean = true,
        guildId: String? = null
    ): List<GuildMember>? = GuildMemberFetchAction(client).handle(data, isSync, guildId)

    fun messageFetch(
        data: JsonElement,
        gotBeginning: Boolean? = false,
        channelId: String? = null
    ): List<Message>? = MessageFetchAction(client).handle(data, gotBeginning, channelId)

    fun messageCreate(
        data: JsonElement
    ): Message? = MessageCreateAction(client).handle(data)

    fun messageDelete(data: JsonElement)
            : Message? = MessageDeleteAction(client).handle(data)

    fun messageUpdate(data: JsonElement): Message? = MessageUpdateAction(client).handle(data)

    fun reactionRemove(data: JsonElement): MessageReaction? =
        ReactionRemoveAction(client).handle(data)

    fun reactionAdd(data: JsonElement): MessageReaction? =
        ReactionAddAction(client).handle(data)

    fun presenceFetch(
        data: JsonElement,
        guildId: String? = null
    ): List<Presence>? = PresenceFetchAction(client).handle(data, guildId)

    fun presenceUpdate(data: JsonObject): Presence? = PresenceUpdateAction(client).handle(data)

    fun guildMemberUpdate(data: JsonObject): GuildMember? = GuildMemberUpdateAction(client).handle(data)

    fun guildSettingsUpdate(data: JsonObject): GuildSettings? =
        GuildSettingsUpdateAction(client).handle(data)

    fun joinVoiceChannel(data:JsonObject):VoiceConnectStateReceive? = JoinVoiceChannelAction(client).handle(data)
}