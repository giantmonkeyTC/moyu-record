package cn.troph.tomon.core.utils

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

class SnowFlakesGenerator(private val machineId: Int)  {
    private val ATOMIC_INCREMENT = AtomicInteger(0)
    private val EPOCH = 1559347200000L
    private val MAX_MACHINE_ID = 64
    private val ALPHA_NUMERIC_BASE = 36
    private val TIME_STAMP_SHIFT = 22
    private val MACHINE_ID_SHIFT = 16

    init {
        if (machineId >= MAX_MACHINE_ID || machineId < 0) {
            throw IllegalArgumentException("Machine Number must between 0 - ${MAX_MACHINE_ID - 1}")
        }
    }


    fun nextId(): Long {
        synchronized(this) {
            val currentTs = System.currentTimeMillis()
            val ts = currentTs - EPOCH
            val maxIncrement = 16384
            val max = maxIncrement - 2

            if (ATOMIC_INCREMENT.get() >= max) {
                ATOMIC_INCREMENT.set(0)
            }
            val i = ATOMIC_INCREMENT.incrementAndGet()
            return (ts shl TIME_STAMP_SHIFT) or (this.machineId shl MACHINE_ID_SHIFT).toLong() or i.toLong()
        }
    }

    fun nextAlpha(): String {
        val id = nextId()
        return java.lang.Long.toString(id, ALPHA_NUMERIC_BASE)
    }


}

data class SnowFlakeId(
    val timestamp: LocalDateTime,
    val machineId: Int,
    val increment: Int
)