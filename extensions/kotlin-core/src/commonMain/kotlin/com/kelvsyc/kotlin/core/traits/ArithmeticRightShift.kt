package com.kelvsyc.kotlin.core.traits

/**
 * Trait class denoting that a class supports an arithmetic right shift operation.
 *
 * This arithmetic right shift is a "saturated right shift", where a bit shift in excess of the size of the type, in
 * bits, returns a zero. This behavior is different from the standard `shr` infix function, which only consider the
 * lower order bits of the bit count. (eg. [Int.shr] only considers the lower five bits of its bit count argument).
 *
 * Implementations are not required to support a negative right shift.
 */
interface ArithmeticRightShift<T> {
    /**
     * Shifts the specified value right by the specified number of bits.
     *
     * Implementations are not required to support a negative right shift.
     */
    fun arithmeticRightShift(value: T, bitCount: Int): T
}
