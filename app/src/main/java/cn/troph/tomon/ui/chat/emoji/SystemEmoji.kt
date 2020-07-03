package cn.troph.tomon.ui.chat.emoji

import android.content.Context
import cn.troph.tomon.R
import cn.troph.tomon.core.utils.FileUtils
import com.alibaba.sdk.android.ams.common.util.FileUtil
import com.google.gson.Gson

class SystemEmoji(private val context: Context) {

    fun returnEmojiWithCategory(): HashMap<String, MutableList<SystemEmojiData>> {
        val map = HashMap<String, MutableList<SystemEmojiData>>()
        val emojiRawList =
            Gson().fromJson(
                FileUtils.loadJSONFromAsset(context, "emoji_trimmed.json"),
                Array<SystemEmojiData>::class.java
            ).toMutableList()
        for (item in emojiRawList) {
            if (map.containsKey(item.category)) {
                map[item.category]?.add(item)
            } else {
                val list = mutableListOf<SystemEmojiData>()
                list.add(item)
                map[item.category] = list
            }
        }
        return map
    }

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
        return CustomGuildEmoji(name = "Symbol", isBuildIn = true, systemEmojiList = list)
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