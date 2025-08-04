package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface extending [DecimalFloatingPointTraits] denoting that a type is a `decimal32` floating-point type.
 */
interface Decimal32Traits<T> : DecimalFloatingPointTraits<T>, Sized<T>
