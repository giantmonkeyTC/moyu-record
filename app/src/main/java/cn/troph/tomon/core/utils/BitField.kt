package cn.troph.tomon.core.utils

open class BitField {
    var value: Int = 0

    constructor(b: Any) {
        value = resolve(b)
    }

    fun plus(b: BitField): BitField {
        return BitField(value or b.value)
    }

    fun minus(b: BitField): BitField {
        return BitField(value and b.value.inv())
    }

    fun equals(b: BitField): Boolean {
        return value == b.value
    }

    override fun hashCode(): Int {
        return value
    }

    private fun resolve(b: Any): Int {
        if (b is Int) {
            return b;
        }
        if (b is BitField) {
            return b.value;
        }
        if (b is Iterable<*>) {
            return b.map { p -> resolve(p!!) }.fold(0) { prev, p -> prev or p };
        }
        throw IllegalArgumentException("unsolved input")
    }
}