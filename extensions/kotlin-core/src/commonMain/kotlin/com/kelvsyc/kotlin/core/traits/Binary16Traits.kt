package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface extending [FloatingPointTraits] denoting that a type is a `binary16` floating-point type.
 */
interface Binary16Traits<T> : FloatingPointTraits<T>, Sized
