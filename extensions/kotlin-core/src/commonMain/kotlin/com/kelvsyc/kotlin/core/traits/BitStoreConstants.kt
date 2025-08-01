package com.kelvsyc.kotlin.core.traits

/**
 * Traits interface providing constants for a particular bit store type.
 */
interface BitStoreConstants<T> {
    /**
     * A value corresponding to all bits being set.
     */
    val allSet: T

    /**
     * A value corresponding to all bits being clear.
     */
    val allClear: T

    /**
     * Returns `true` if all bits in the supplied value are clear.
     */
    fun isAllClear(value: T): Boolean

    /**
     * Returns `true` if at least one bit in the supplied value is set.
     */
    fun hasSetBits(value: T): Boolean
}
