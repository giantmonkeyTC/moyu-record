package cn.troph.tomon.core.utils

import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class SortedList<T>(
    private val comparator: Comparator<T>,
    l: List<T>? = null
) :
    Iterable<T> {

    private var list: MutableList<T> = CopyOnWriteArrayList(l?.toMutableList() ?: mutableListOf())

    init {
        list.sortedWith(comparator)
    }

    val size: Int get() = list.size

    fun add(element: T) = findIndex(element).let { index ->
        list.add(if (index < 0) -(index + 1) else index, element)
    }

    fun addIfNotExist(element: T) = findIndex(element).let { index ->
        if (index < 0) {
            list.add(-(index + 1), element)
        }
    }

    fun remove(element: T) = findIndex(element).let { index ->
        if (index >= 0) list.removeAt(index)
    }

    operator fun get(index: Int): T = list[index]

    fun contains(element: T) = findIndex(element).let { index ->
        index >= 0 && element == list[index] || (findEquals(index + 1, element, 1) || findEquals(
            index - 1,
            element,
            -1
        ))
    }

    override fun iterator(): Iterator<T> = list.iterator()

    private fun findIndex(element: T): Int = list.binarySearch(element, comparator)

    private tailrec fun findEquals(index: Int, element: T, step: Int): Boolean = when {
        index !in 0 until size -> false
        comparator.compare(element, list[index]) != 0 -> false
        list[index] == element -> true
        else -> findEquals(index + step, element, step)
    }

    fun toList(): MutableList<T> {
        return list
    }

}