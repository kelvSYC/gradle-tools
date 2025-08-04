package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface extending [DecimalFloatingPointTraits] denoting that a type is a `decimal128` floating-point type.
 */
interface Decimal128Traits<T> : DecimalFloatingPointTraits<T>, Sized<T>
