package cn.troph.tomon.core

import cn.troph.tomon.core.collections.UserCollection

class Client {
    val users = UserCollection(this)
}