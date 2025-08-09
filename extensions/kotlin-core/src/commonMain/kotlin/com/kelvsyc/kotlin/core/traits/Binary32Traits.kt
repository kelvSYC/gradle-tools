package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface extending [FloatingPointTraits] denoting that a type is a `binary32` floating-point type (such as
 * [Float]).
 */
interface Binary32Traits<T> : FloatingPointTraits<T>, Sized
