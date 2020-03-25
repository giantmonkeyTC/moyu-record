package cn.troph.tomon.core.utils

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
}