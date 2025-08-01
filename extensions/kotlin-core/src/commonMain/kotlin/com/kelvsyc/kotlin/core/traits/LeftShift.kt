package com.kelvsyc.kotlin.core.traits

/**
 * Trait class denoting that a class supports a left shift operation.
 *
 * This left shift is a "saturated left shift", where a bit shift in excess of the size of the type, in bits, returns a
 * zero. This behavior is different from the standard `shl` infix function, which only consider the lower order bits
 * of the bit count. (eg. [Int.shl] only considers the lower five bits of its bit count argument).
 *
 * Implementations are not required to support a negative left shift.
 */
interface LeftShift<T> {
    /**
     * Shifts the specified value left by the specified number of bits.
     *
     * Implementations are not required to support a negative left shift.
     */
    fun leftShift(value: T, bitCount: Int): T
}
