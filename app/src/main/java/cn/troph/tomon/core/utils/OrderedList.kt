package cn.troph.tomon.core.utils

import java.util.*

class OrderedList<T : Comparable<T>>(
    l: List<T>? = null,
    private val comparator: Comparator<T> = Comparator { o1, o2 ->
        o1.compareTo(o2)
    }
) :
    Iterable<T> {

    private var list: LinkedList<T> = LinkedList(l ?: listOf())

    init {
        list.sortedWith(comparator)
    }

    operator fun get(index: Int): T = list[index]

    fun safeGet(index: Int): T? = if (index < 0 || index >= list.size) null else get(index)

    fun add(value: T) {
        val invertedInsertionPoint = list.binarySearch(value, comparator)
        val actualInsertionPoint = -(invertedInsertionPoint + 1)
        list.add(actualInsertionPoint, value)
    }

    fun remove(value: T) {
        val index = list.binarySearch(value, comparator)
        if (index >= 0 && index < list.size) {
            list.removeAt(index)
        }
    }

    override fun iterator(): Iterator<T> = list.iterator()

}