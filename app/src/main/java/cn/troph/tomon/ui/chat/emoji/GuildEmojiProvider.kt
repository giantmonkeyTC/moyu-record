package cn.troph.tomon.ui.chat.emoji

import cn.troph.tomon.core.Client
import com.vanniktech.emoji.EmojiProvider
import com.vanniktech.emoji.emoji.EmojiCategory
import com.vanniktech.emoji.ios.category.*


class GuildEmojiProvider : EmojiProvider {

    override fun getCategories(): Array<EmojiCategory> {
        val guildEmojiList = mutableListOf<EmojiCategory>()
        for (eachGuild in Client.global.guilds.list) {
            guildEmojiList.add(GuildEmojiCategory(eachGuild))
        }
        val systemEmoji = arrayOf(
            SmileysAndPeopleCategory(),
            AnimalsAndNatureCategory(),
            FoodAndDrinkCategory(),
            ActivitiesCategory(),
            TravelAndPlacesCategory(),
            ObjectsCategory(),
            SymbolsCategory(),
            FlagsCategory()
        )
        guildEmojiList.addAll(systemEmoji)
        return guildEmojiList.toTypedArray()
    }
}