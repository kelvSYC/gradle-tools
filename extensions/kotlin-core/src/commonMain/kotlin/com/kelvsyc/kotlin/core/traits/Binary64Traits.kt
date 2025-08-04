package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface extending [FloatingPointTraits] denoting that a type is a `binary64` floating-point type (such as
 * [Double]).
 */
interface Binary64Traits<T> : FloatingPointTraits<T>, Sized<T>
