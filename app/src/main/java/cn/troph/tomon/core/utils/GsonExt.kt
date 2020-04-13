package cn.troph.tomon.core.utils

import com.google.gson.*
import java.math.BigDecimal
import java.math.BigInteger

enum class GsonConflictStrategy {
    THROW_EXCEPTION, PREFER_FIRST_OBJ, PREFER_SECOND_OBJ, PREFER_NON_NULL
}

class JsonObjectExtensionConflictException(message: String) : Exception(message)

fun JsonObject.merge(
    obj: JsonObject,
    conflictStrategy: GsonConflictStrategy
) {
    obj.entrySet().forEach { entry ->
        val key = entry.key
        val value = entry.value
        if (has(key)) {
            val leftVal = get(key)
            handleMergeConflict(key, this, leftVal, value, conflictStrategy)
        } else {
            add(key, value)
        }
    }
}

private fun handleMergeConflict(
    key: String,
    leftObj: JsonObject,
    leftVal: JsonElement,
    rightVal: JsonElement,
    conflictStrategy: GsonConflictStrategy
) {
    when (conflictStrategy) {
        GsonConflictStrategy.PREFER_FIRST_OBJ -> return
        GsonConflictStrategy.PREFER_SECOND_OBJ -> leftObj.add(key, rightVal)
        GsonConflictStrategy.PREFER_NON_NULL -> {
            if (leftVal.isJsonNull && !rightVal.isJsonNull) {
                leftObj.add(key, rightVal)
            }
        }
        GsonConflictStrategy.THROW_EXCEPTION -> throw JsonObjectExtensionConflictException("Key $key")
    }
}

val JsonElement.optString: String?
    get() = safeConversion { asString }

val JsonElement.optLong: Long?
    get() = safeConversion { asLong }

val JsonElement.optBoolean: Boolean?
    get() = safeConversion { asBoolean }

val JsonElement.optFloat: Float?
    get() = safeConversion { asFloat }

val JsonElement.optDouble: Double?
    get() = safeConversion { asDouble }

val JsonElement.optJsonObject: JsonObject?
    get() = safeConversion { asJsonObject }

val JsonElement.optJsonArray: JsonArray?
    get() = safeConversion { asJsonArray }

val JsonElement.optJsonPrimitive: JsonPrimitive?
    get() = safeConversion { asJsonPrimitive }

val JsonElement.optInt: Int?
    get() = safeConversion { asInt }

val JsonElement.optBigDecimal: BigDecimal?
    get() = safeConversion { asBigDecimal }

val JsonElement.optBigInteger: BigInteger?
    get() = safeConversion { asBigInteger }

val JsonElement.optByte: Byte?
    get() = safeConversion { asByte }

val JsonElement.optShort: Short?
    get() = safeConversion { asShort }

val JsonElement.optJsonNull: JsonNull?
    get() = safeConversion { asJsonNull }

val JsonElement.optCharacter: Char?
    get() = safeConversion { asCharacter }

private fun <T> JsonElement.safeConversion(converter: () -> T?): T? {

    return try {
        converter()
    } catch (e: Exception) {
        null
    }
}