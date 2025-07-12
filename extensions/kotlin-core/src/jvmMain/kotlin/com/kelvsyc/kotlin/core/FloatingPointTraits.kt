package com.kelvsyc.kotlin.core

import kotlin.Double as KDouble
import kotlin.Float as KFloat

/**
 * Object holder for traits specific to Java floating point types.
 *
 * These are separate from [TypeTraits] due to the presence of multiple implementations.
 */
object FloatingPointTraits {
    object Float : FusedMultiplyAdd<KFloat> {
        override fun fma(a: KFloat, b: KFloat, c: KFloat): KFloat = Math.fma(a, b, c)
    }

    object StrictFloat : FusedMultiplyAdd<KFloat> {
        override fun fma(a: KFloat, b: KFloat, c: KFloat): KFloat = StrictMath.fma(a, b, c)
    }

    object Double : FusedMultiplyAdd<KDouble> {
        override fun fma(a: KDouble, b: KDouble, c: KDouble): KDouble = Math.fma(a, b, c)
    }

    object StrictDouble : FusedMultiplyAdd<KDouble> {
        override fun fma(a: KDouble, b: KDouble, c: KDouble): KDouble = StrictMath.fma(a, b, c)
    }
}
