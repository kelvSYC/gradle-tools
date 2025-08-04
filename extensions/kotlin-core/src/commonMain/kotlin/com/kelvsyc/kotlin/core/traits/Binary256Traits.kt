package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface extending [FloatingPointTraits] denoting that a type is a `binary256` floating-point type.
 */
interface Binary256Traits<T> : FloatingPointTraits<T>, Sized<T>
