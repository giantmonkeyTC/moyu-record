package cn.troph.tomon.core.utils

import com.google.gson.JsonPrimitive
import java.time.LocalDateTime
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
            is String -> LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
            is JsonPrimitive -> LocalDateTime.parse(value.asString, DateTimeFormatter.ISO_DATE_TIME)
            else -> LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_DATE_TIME)
        }
    }
}