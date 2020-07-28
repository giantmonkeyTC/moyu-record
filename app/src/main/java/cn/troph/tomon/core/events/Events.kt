package cn.troph.tomon.core.events

import cn.troph.tomon.core.actions.Position
import cn.troph.tomon.core.structures.*

open class Event
class UserLoginEvent : Event()
class UserLogoutEvent : Event()
data class UserUpdateEvent(val user: User) : Event()

class GuildSyncEvent : Event()
data class GuildFetchEvent(val guilds: List<Guild>) : Event()
data class GuildCreateEvent(val guild: Guild) : Event()
data class GuildDeleteEvent(val guild: Guild) : Event()
data class GuildUpdateEvent(val guild: Guild) : Event()
data class GuildPositionEvent(val guilds: List<Position>) : Event()

class ChannelSyncEvent(val guild: Guild?) : Event()
data class ChannelFetchEvent(val channels: List<Channel>) : Event()
data class ChannelCreateEvent(val channel: Channel) : Event()
data class ChannelDeleteEvent(val channel: Channel) : Event()
data class ChannelUpdateEvent(val channel: Channel) : Event()
data class GuildChannelPositionEvent(val guild: Guild) : Event()
data class ChannelAckEvent(val channel: Channel) : Event()
data class ChannelTypingEvent(val channel: Channel) : Event()
data class ChannelMemberUpdateEvent(val channel: Channel) : Event()
data class DmChannelCreateEvent(val channel: DmChannel) : Event()

data class RoleSyncEvent(val guild: Guild) : Event()
data class RoleFetchEvent(val roles: List<Role>) : Event()
data class RoleCreateEvent(val role: Role) : Event()
data class RoleDeleteEvent(val role: Role) : Event()
data class RoleUpdateEvent(val role: Role) : Event()
data class RolePositionEvent(val roles: List<Role>) : Event()

data class EmojiSyncEvent(val guild: Guild) : Event()
data class EmojiFetchEvent(val emojis: List<GuildEmoji>) : Event()
data class EmojiCreateEvent(val emoji: GuildEmoji) : Event()
data class EmojiDeleteEvent(val emoji: GuildEmoji) : Event()
data class EmojiUpdateEvent(val emoji: GuildEmoji) : Event()

data class GuildMemberFetchEvent(val members: List<GuildMember>) : Event()
data class GuildMemberAddEvent(val member: GuildMember) : Event()
data class GuildMemberRemoveEvent(val member: GuildMember) : Event()
data class GuildMemberUpdateEvent(val member: GuildMember) : Event()

data class MessageFetchEvent(val messages: List<Message>) : Event()
data class MessageCreateEvent(val message: Message) : Event()
data class MessageDeleteEvent(val message: Message) : Event()
data class MessageUpdateEvent(val message: Message) : Event()
data class MessageReadEvent(val message: Message) : Event()
data class MessageAtMeEvent(val message: Message) : Event()

data class ReactionRemoveEvent(val reaction: MessageReaction) : Event()
data class ReactionAddEvent(val reaction: MessageReaction) : Event()

data class PresenceFetchEvent(val presences: List<Presence>) : Event()
data class PresenceUpdateEvent(val presence: Presence) : Event()
data class GuildSettingsUpdateEvent(val settings: GuildSettings) : Event()

data class VoiceAllowConnectEvent(val voiceAllowConnect: VoiceAllowConnectReceive):Event()
data class VoiceLeaveChannelEvent(val voiceAllowConnect: VoiceAllowConnectReceive):Event()

data class VoiceSpeakEvent(val speaking: Speaking) : Event()
data class VoiceSocketStateEvent(val isOpen:Boolean): Event()
