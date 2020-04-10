package cn.troph.tomon.core.utils

object Snowflake {
    fun aligned(snowflake: String): String {
        val length = snowflake.length
        val align = (20 - length).coerceAtLeast(0)
        return "${"0".repeat(align)}$snowflake"
    }
}