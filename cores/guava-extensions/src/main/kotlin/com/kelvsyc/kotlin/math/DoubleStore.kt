package com.kelvsyc.kotlin.math

/**
 * Extension of a [FloatingPointStore] for the [Double] type.
 */
interface DoubleStore<S : BitStore<S, R>, R> : FloatingPointStore<S, R> {
    /**
     * Base class representing metadata relating to a [DoubleStore].
     */
    abstract class AbstractCompanion<F : DoubleStore<S, R>, S : BitStore<S, R>, R> : FloatingPointStore.AbstractCompanion<F, S, R>() {
        override val sizeBits by Double.Companion::SIZE_BITS
        override val precision by java.lang.Double::PRECISION

        /**
         * Creates a store from the specified [Double] value.
         */
        abstract fun create(value: Double): F
    }

    /**
     * Converts this store to a [Double] value.
     */
    fun toDouble(): Double
}
