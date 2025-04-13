package com.kelvsyc.kotlin.math

/**
 * Extension of a [FloatingPointStore] for the [Float] type.
 */
interface FloatStore<S : BitStore<S, R>, R> : FloatingPointStore<S, R> {
    /**
     * Base class representing metadata relating to a [FloatStore].
     */
    abstract class AbstractCompanion<F : FloatStore<S, R>, S : BitStore<S, R>, R> : FloatingPointStore.AbstractCompanion<F, S, R>() {
        override val sizeBits by Float.Companion::SIZE_BITS
        override val precision by java.lang.Float::PRECISION

        /**
         * Creates a store from the specified [Float] value.
         */
        abstract fun create(value: Float): F
    }

    /**
     * Converts this store to a [Float] value.
     */
    fun toFloat(): Float
}
