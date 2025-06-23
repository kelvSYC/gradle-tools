package com.kelvsyc.kotlin.guava

import com.google.common.primitives.UnsignedInteger

/**
 * Returns this value as a Kotlin [UInt]
 */
val UnsignedInteger.asUInt
    get() = toInt().toUInt()

/**
 * Returns this value as a Guava [UnsignedInteger].
 */
val UInt.asGuavaUnsignedInteger
    get() = UnsignedInteger.fromIntBits(toInt())

/**
 * Syntactic sugar to enable the `/` operator when working with Guava [UnsignedInteger]
 *
 * @see UnsignedInteger.dividedBy
 */
operator fun UnsignedInteger.div(other: UnsignedInteger) = dividedBy(other)

/**
 * Syntactic sugar to enable the `%` operator when working with Guava [UnsignedInteger]
 *
 * @see UnsignedInteger.mod
 */
operator fun UnsignedInteger.rem(other: UnsignedInteger) = mod(other)
