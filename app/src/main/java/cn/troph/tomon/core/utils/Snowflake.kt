package cn.troph.tomon.core.utils

class Snowflake(val value: String = ""): Comparable<Snowflake> {

    val aligned: String get() {
        val length = value.length
        val align = (20 - length).coerceAtLeast(0)
        return "${"0".repeat(align)}$value"
    }

    val long: Long get() {
        return value.toLongOrNull() ?: 0L
    }

    override fun compareTo(other: Snowflake): Int {
        return aligned.compareTo(other.aligned)
    }
}

val String.snowflake: Snowflake get() {
    return Snowflake(this)
}