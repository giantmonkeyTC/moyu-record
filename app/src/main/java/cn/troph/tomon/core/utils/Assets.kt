package cn.troph.tomon.core.utils

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

    val regexContent: Regex = Regex("""<%[\w\u4e00-\u9fa5]+:[0-9]+>""")
    fun contentParser(content: String): List<ContentEmoji> {
        val regexRaw = Regex("""\:""")
        val listMatches = regexContent.findAll(content, 0).toList()
        val listContentEmoji = mutableListOf<ContentEmoji>()
        listContentEmoji.clear()
        listMatches.forEach { result ->
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
        return listContentEmoji


    }

}