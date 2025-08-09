package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface extending [DecimalFloatingPointTraits] denoting that a type is a `decimal64` floating-point type.
 */
interface Decimal64Traits<T> : DecimalFloatingPointTraits<T>, Sized
