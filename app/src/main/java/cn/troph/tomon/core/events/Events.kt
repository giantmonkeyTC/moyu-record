package cn.troph.tomon.core.events

import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.structures.GuildMember
import cn.troph.tomon.core.structures.Me
import cn.troph.tomon.core.structures.User

open class Event
class UserLoginEvent : Event()
class UserLogoutEvent : Event()
data class UserUpdateEvent(val user: User) : Event()
data class UserFetchEvent(val user: User?) : Event()
data class UserRegisterEvent(val me: Me) : Event()
class GuildSyncEvent : Event()
data class GuildFetchEvent(val guilds: List<Guild>) : Event()
data class GuildCreateEvent(val guild: Guild) : Event()
data class GuildDeleteEvent(val guild: Guild) : Event()
data class GuildUpdateEvent(val guild: Guild) : Event()
data class GuildMemberFetchEvent(val guildMembers: List<GuildMember>) : Event()
data class GuildMemberAddEvent(val guildMember: GuildMember) : Event()
data class GuildMemberRemoveEvent(val guildMember: GuildMember) : Event()
data class GuildMemberUpdateEvent(val guildMember: GuildMember) : Event()

