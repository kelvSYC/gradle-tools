package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface extending [FloatingPointTraits] denoting that a type is a `binary128` floating-point type.
 */
interface Binary128Traits<T> : FloatingPointTraits<T>, Sized
