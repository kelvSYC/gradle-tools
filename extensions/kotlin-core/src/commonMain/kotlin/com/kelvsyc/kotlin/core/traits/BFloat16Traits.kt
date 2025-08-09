package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface extending [FloatingPointTraits] denoting that a type is a `bfloat16` floating-point type.
 */
interface BFloat16Traits<T> : FloatingPointTraits<T>, Sized
