package com.kelvsyc.kotlin.core.traits

/**
 * Trait class denoting that the type supports a "sticky right shift" operation.
 *
 * A "sticky right shift" operation is a logical right shift operation, except that if any of the shifted-off bits is a
 * `1`, the least significant bit of the result is also a `1`.
 *
 * @param T Type supporting the sticky right shift operation.
 */
interface StickyRightShift<T> {
    /**
     * Performs a sticky right shift operation.
     */
    fun stickyRightShift(value: T, bitCount: Int): T
}
