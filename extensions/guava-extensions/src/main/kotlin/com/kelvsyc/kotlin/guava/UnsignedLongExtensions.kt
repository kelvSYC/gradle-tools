package com.kelvsyc.kotlin.guava

import com.google.common.primitives.UnsignedLong

/**
 * Returns this value as a Kotlin [ULong].
 */
val UnsignedLong.asULong
    get() = toLong().toULong()

/**
 * Returns this value as a Guava [UnsignedLong].
 */
val ULong.asGuavaUnsignedLong
    get() = UnsignedLong.fromLongBits(toLong())

/**
 * Syntactic sugar to enable the `/` operator when working with Guava [UnsignedLong]
 *
 * @see UnsignedLong.dividedBy
 */
operator fun UnsignedLong.div(other: UnsignedLong) = dividedBy(other)

/**
 * Syntactic sugar to enable the `%` operator when working with Guava [UnsignedLong]
 *
 * @see UnsignedLong.mod
 */
operator fun UnsignedLong.rem(other: UnsignedLong) = mod(other)
