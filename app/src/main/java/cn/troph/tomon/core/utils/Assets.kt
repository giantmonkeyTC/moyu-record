package cn.troph.tomon.core.utils

import cn.troph.tomon.core.Client

object Assets {
    const val defaultStampPackId = "138854573120528384"
    fun emojiURL(id: String, animated: Boolean = false): String {
        return "https://cdn.tomon.co/emojis/$id.${if (animated) "gif" else "png"}${if (animated) "" else "?x-oss-process=image/resize,p_50"}"
    }

    fun iconURL(id: String): String {
        return "https://cdn.tomon.co/icons/$id"
    }

    data class ContentEmoji(
        val start: Int,
        val end: Int,
        val raw: String,
        val id: String,
        val name: String
    )

    data class ContentAtUser(
        val start: Int,
        val end: Int,
        val name: String,
        val id: String
    )

    data class ContentUser(
        val name: String,
        val id: String
    )

    data class ContentSpan(
        val contentAtUser: List<ContentAtUser>,
        val contentEmoji: List<ContentEmoji>,
        val parseContent: String
    )

    val regexEmoji: Regex = Regex("""<%[\w\u4e00-\u9fa5]+:[0-9]+>""")
    val regexAtUser: Regex = Regex("""<@[0-9]+>""")
    val regexMention: Regex = Regex("""@[\w\u4e00-\u9fa5]+#[0-9]{4}""")
    val regexLink: Regex = Regex("""http:\/\/\S+""")
    val regexLinkHttps: Regex = Regex("""https:\/\/\S+""")
    fun contentParser(content: String): ContentSpan {
        val regexRaw = Regex("""\:""")
        val users = mutableListOf<ContentUser>()
        val parserContent =
            content.replace(
                regexAtUser
            ) {
                val name =
                    Client.global.users[it.value.substring(2, it.value.length - 1)]?.name ?: ""
                users.add(ContentUser(name = name, id = it.value.substring(2, it.value.length - 1)))
                "@$name"
            }
        var index = 0
        val listAtUser = mutableListOf<ContentAtUser>()
        listAtUser.clear()
        users.forEach {
            val start = parserContent.indexOf("@${it.name}", index)
            index += it.name.length
            val contentAtUser = ContentAtUser(
                start = start,
                end = start + it.name.length,
                name = it.name,
                id = it.id
            )
            listAtUser.add(contentAtUser)
        }
        val emojiListMatches = regexEmoji.findAll(parserContent, 0).toList()
        val listContentEmoji = mutableListOf<ContentEmoji>()
        listContentEmoji.clear()
        emojiListMatches.forEach { result ->
            val list = regexRaw.split(result.value)
            val contentEmoji = ContentEmoji(
                start = result.range.first,
                end = result.range.last,
                raw = result.value,
                name = list[0].substring(2),
                id = list[1].substring(0, list[1].length - 1)
            )
            listContentEmoji.add(contentEmoji)
        }
        return ContentSpan(
            contentAtUser = listAtUser,
            contentEmoji = listContentEmoji,
            parseContent = parserContent
        )
    }

    fun mentionSendParser(content: String): String {
        val parserContent =
            content.replace(
                regexMention
            ) {
                val id =
                    Client.global.users.findWithIdentifier(it.value.substring(1))?.id
                "<@$id>"
            }
        return parserContent
    }


}