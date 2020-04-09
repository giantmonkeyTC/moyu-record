package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Emoji

class EmojiCollection(client: Client, m: Map<String, Emoji>? = null) :
    BaseCollection<Emoji>(client, m) {

}