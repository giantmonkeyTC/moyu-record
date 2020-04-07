package cn.troph.tomon.core.events

import cn.troph.tomon.core.structures.User

open class Event
class UserLoginEvent: Event()
class UserLogoutEvent: Event()
data class UserUpdateEvent(val user: User): Event()