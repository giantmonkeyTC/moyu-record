package cn.troph.tomon.ui.chat.emoji

class SystemEmoji {

    fun getSystemEmojiEmoticons(): CustomGuildEmoji {
        val list = mutableListOf<Int>()
        for (i in 0x1F601..0x1F64F) {
            list.add(i)
        }
        return CustomGuildEmoji(name = "Emoticons", isBuildIn = true, systemEmojiList = list)
    }

    fun getSystemEmojiDingbats(): CustomGuildEmoji {
        val list = mutableListOf<Int>()
        for (i in 0x2702..0x27B0) {
            list.add(i)
        }
        return CustomGuildEmoji(name = "DingBats", isBuildIn = true, systemEmojiList = list)
    }

    fun getSystemEmojiTransport(): CustomGuildEmoji {
        val list = mutableListOf<Int>()
        for (i in 0x1F680..0x1F6C0) {
            list.add(i)
        }
        return CustomGuildEmoji(name = "Transport", isBuildIn = true, systemEmojiList = list)
    }

    fun getSystemEmojiSymbol(): CustomGuildEmoji {
        val list = mutableListOf<Int>()
        for (i in 0x24C2..0x1F251) {
            list.add(i)
        }
        return CustomGuildEmoji(name = "Symbol", isBuildIn = true, systemEmojiList = list)
    }

}