package com.kelvsyc.kotlin.core.traits

/**
 * Trait class denoting that a class supports a logical right shift operation.
 *
 * This logical right shift is a "saturated right shift", where a bit shift in excess of the size of the type, in bits,
 * returns a zero. This behavior is different from the standard `shr` or `ushr` infix function, which only consider the
 * lower order bits of the bit count. (eg. [Int.ushr] only considers the lower five bits of its bit count argument).
 *
 * Implementations are not required to support a negative right shift.
 */
interface RightShift<T> {
    /**
     * Shifts the specified value right by the specified number of bits.
     *
     * Implementations are not required to support a negative right shift.
     */
    fun rightShift(value: T, bitCount: Int): T
}
