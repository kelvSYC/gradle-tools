package com.kelvsyc.kotlin.core.traits

/**
 * Trait class denoting that a class supports a "rounding right shift" operation.
 *
 * A rounding right shift operation is a logical right shift operation, except that any shifted-off bits contribute to
 * the result being rounded using a half-even method.
 */
interface RoundingRightShift<T> {
    /**
     * Performs a rounding right shift operation.
     *
     * Implementations are not required to support a negative rounding right shift.
     */
    fun roundingRightShift(value: T, bitCount: Int): T
}
