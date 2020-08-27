package cn.troph.tomon.core.utils

import androidx.emoji.widget.EmojiTextView
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import com.google.gson.JsonPrimitive
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.item_message_reply.view.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Converter {
    fun toInt(value: Any?): Int {
        return when (value) {
            is Int -> value as Int
            is Double -> value.toInt()
            is String -> value.toInt()
            else -> 0
        }
    }

    fun toLong(value: Any?): Long {
        return when (value) {
            is Int -> value.toLong()
            is Long -> value as Long
            is Double -> value.toLong()
            is String -> value.toLong()
            else -> 0L
        }
    }

    fun toDate(value: Any?): LocalDateTime {
        if (value == null) {
            return LocalDateTime.now()
        }
        return when (value) {
            is String -> {
                if (value.isNotEmpty()) {

                    LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                        .atZone(ZoneId.of("GMT+0")).withZoneSameInstant(
                            ZoneId.systemDefault()
                        ).toLocalDateTime()
                } else {
                    LocalDateTime.now()
                }
            }
            is JsonPrimitive -> LocalDateTime.parse(value.asString, DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneId.of("GMT+0")).withZoneSameInstant(
                    ZoneId.systemDefault()
                ).toLocalDateTime()
            else -> LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneId.of("GMT+0")).withZoneSameInstant(
                    ZoneId.systemDefault()
                ).toLocalDateTime()
        }
    }

    fun toMarkdownTextView(markdown: Markwon?, content: String, itemView: EmojiTextView) {
        if (content != null && (Assets.regexEmoji.containsMatchIn(content!!) || Assets.regexAtUser.containsMatchIn(
                content!!
            ))
        ) {
            val contentSpan = Assets.contentParser(content!!)
            var tempMsg = content
            contentSpan.contentEmoji.forEach {
                tempMsg = tempMsg?.replaceFirst(
                    it.raw,
                    "<img src=\"%s\" height='70' />".format(Assets.emojiURL(it.id))
                )
            }

            val contentSpanAtUser = Assets.contentParser(tempMsg!!)
            val atUserTemplate = "<tomonandroid>%s</tomonandroid>"
            contentSpanAtUser.contentAtUser.forEach {
                tempMsg = tempMsg?.replaceFirst(
                    "<@${it.id}>",
                    atUserTemplate.format("@${it.name}#${Client.global.users[it.id]?.discriminator}")
                )
            }
            markdown?.setMarkdown(
                itemView,
                Assets.regexReturn.replace(tempMsg ?: "") {
                    "<br>"
                })
        } else {
            if (Assets.regexReturn.containsMatchIn(content ?: "")) {
                val display = content
                markdown?.setMarkdown(
                    itemView,
                    Assets.regexReturn.replace(display ?: "") {
                        "<br>"
                    })
            } else
                markdown?.setMarkdown(
                    itemView,
                    content ?: ""
                )

        }
    }

}