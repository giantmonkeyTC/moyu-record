package cn.troph.tomon.core.utils

import cn.troph.tomon.core.Client

object Assets {
    fun emojiURL(id: String, animated: Boolean = false): String {
        return "https://cdn.tomon.co/emojis/$id.${if (animated) "gif" else "png"}"
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
        val name: String
    )

    data class ContentSpan(
        val contentAtUser: List<ContentAtUser>,
        val contentEmoji: List<ContentEmoji>,
        val parseContent: String
    )

    val regexEmoji: Regex = Regex("""<%[\w\u4e00-\u9fa5]+:[0-9]+>""")
    val regexAtUser: Regex = Regex("""<@[0-9]+>""")
    fun contentParser(content: String): ContentSpan {
        val regexRaw = Regex("""\:""")
        val userNames = mutableListOf<String>()
        val parserContent =
            content.replace(
                regexAtUser
            ) {
                val name =
                    Client.global.users[it.value.substring(2, it.value.length - 1)]?.name ?: ""
                userNames.add(name)
                "@$name"
            }
        var index = 0
        val listAtUser = mutableListOf<ContentAtUser>()
        listAtUser.clear()
        userNames.forEach {
            val start = parserContent.indexOf("@${it}", index)
            index += it.length
            val contentAtUser = ContentAtUser(
                start = start,
                end = start + it.length,
                name = it
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

}