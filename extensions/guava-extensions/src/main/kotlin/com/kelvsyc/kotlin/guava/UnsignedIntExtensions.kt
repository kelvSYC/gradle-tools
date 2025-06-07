package com.kelvsyc.kotlin.guava

import com.google.common.primitives.UnsignedInteger

/**
 * Returns this value as a Kotlin [UInt]
 */
val UnsignedInteger.toUInt
    get() = toInt().toUInt()

/**
 * Returns this value as a Guava [UnsignedInteger].
 */
val UInt.toGuavaUnsignedInt
    get() = UnsignedInteger.fromIntBits(toInt())

/**
 * Syntactic sugar to enable the `/` operator when working with Guava [UnsignedInteger]
 *
 * @see UnsignedInteger.dividedBy
 */
fun UnsignedInteger.div(other: UnsignedInteger) = dividedBy(other)

/**
 * Syntactic sugar to enable the `%` operator when working with Guava [UnsignedInteger]
 *
 * @see UnsignedInteger.mod
 */
fun UnsignedInteger.rem(other: UnsignedInteger) = mod(other)
