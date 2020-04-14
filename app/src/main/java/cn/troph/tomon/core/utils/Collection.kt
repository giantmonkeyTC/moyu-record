package cn.troph.tomon.core.utils

open class Collection<T>(m: Map<String, T>?) {
    private var map: LinkedHashMap<String, T> = LinkedHashMap(m ?: mapOf<String, T>())

    inline fun fromMap(map2 : MutableMap<String,T>) :Collection<T>{
        return Collection(map2)
    }

    fun get(key: String): T? = map[key]

    fun set(key: String, value: T): T? {
        val prev = map[key]
        map[key] = value
        return prev
    }

    fun has(key: String): Boolean = map.containsKey(key)

    fun put(key: String, value: T): T? = set(key, value)

    open fun remove(key: String): T? = map.remove(key)

    fun contains(key: String): Boolean = map.contains(key)

    fun clear() = map.clear()

    val length: Int get() = map.size

    val size: Int get() = map.size

    val values: Iterable<T> get() = map.values

    val keys: Iterable<String> get() = map.keys

    fun <R> fold(initial: R, combine: (acc: R, element: T) -> R): R {
        var accumulator: R = initial
        for (element: T in map.values) {
            accumulator = combine(accumulator, element)
        }
        return accumulator
    }

    fun forEach(each: (element: T) -> Unit) = map.values.forEach(each)

    fun filter(predicate: (element: T) -> Boolean): Collection<T> =
        Collection<T>(map.filter { entry -> predicate(entry.value) })

    fun <F> map(transform: (key: String, value: T) -> F): Iterable<F> =
        map.map { e -> transform(e.key, e.value) }

    fun sweep(predicate: (element: T) -> Boolean) {
        val filtered = map.filter { entry -> predicate(entry.value) }
        filtered.forEach { e -> map.remove(e.key) }
    }

    fun merge(other: Collection<T>) = map.putAll(other.map)

    fun clone(): Collection<T> = Collection<T>(map)

    override fun toString(): String = map.toString()

    fun toMap(): Map<String, T> = LinkedHashMap(map)

}