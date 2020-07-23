package cn.troph.tomon.core.utils

import com.google.gson.annotations.SerializedName

class Snowflake(val value: String = "") : Comparable<Snowflake> {
    companion object {
        const val EPOCH: Long = 1559347200000

        data class res(
            @SerializedName("timestamp") val timestamp: Long,
            @SerializedName("workerId") val workerId: Int,
            @SerializedName("processId") val processId: Int,
            @SerializedName("increment") val increment: Int,
            @SerializedName("binary") val binary: String
        )

        fun deconstruct(snowflake: String): res {
            val binary = idToBinary(snowflake).padStart(64, '0')
            return res(
                timestamp = binary.substring(0, 42).toLong(2) + EPOCH,
                workerId = binary.substring(42, 47).toInt(2),
                processId = binary.substring(47, 52).toInt(2),
                increment = binary.substring(52, 64).toInt(2),
                binary = binary
            )
        }

        fun idToBinary(num: String): String {
            val long = num.toLong()
            val binary = long.toString(2)
//            var bin = ""
//            var high =
//                num.slice(IntRange(0, -10))
//                    .toInt()
//            var low = num.slice()
//            while (low > 0 || high > 0) {
//                bin = low + bin
//                low = Math.floor(low / 2)
//                if (high > 0) {
//                    low = low + 5000000000 * (high % 2)
//                    high = Math.floor((high / 2).toDouble()).toInt()
//                }
//            }
            return binary
        }
    }

    val aligned: String
        get() {
            val length = value.length
            val align = (20 - length).coerceAtLeast(0)
            return "${"0".repeat(align)}$value"
        }

    val long: Long
        get() {
            return value.toLongOrNull() ?: 0L
        }

    override fun compareTo(other: Snowflake): Int {
        return aligned.compareTo(other.aligned)
    }
}

val String.snowflake: Snowflake
    get() {
        return Snowflake(this)
    }