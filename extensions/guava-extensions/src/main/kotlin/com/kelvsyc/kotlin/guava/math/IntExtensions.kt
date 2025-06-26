package com.kelvsyc.kotlin.guava.math

import com.google.common.math.IntMath
import java.math.RoundingMode

/**
 * Returns the base 10 logarithm of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see IntMath.log10
 */
fun Int.log10(mode: RoundingMode) = IntMath.log10(this, mode)

/**
 * Returns the base 2 logarithm of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see IntMath.log2
 */
fun Int.log2(mode: RoundingMode) = IntMath.log2(this, mode)

/**
 * Returns the square root of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see IntMath.sqrt
 */
fun Int.sqrt(mode: RoundingMode) = IntMath.sqrt(this, mode)

/**
 * Returns the smallest power of two greater than or equal to this value.
 *
 * @see IntMath.ceilingPowerOfTwo
 */
val Int.ceilingPowerOfTwo
    get() = IntMath.ceilingPowerOfTwo(this)

/**
 * Returns the largest power of two less than or equal to this value.
 *
 * @see IntMath.floorPowerOfTwo
 */
val Int.floorPowerOfTwo
    get() = IntMath.floorPowerOfTwo(this)

/**
 * Returns `true` if this value represents a power of two.
 *
 * @see IntMath.isPowerOfTwo
 */
val Int.isPowerOfTwo
    get() = IntMath.isPowerOfTwo(this)

/**
 * Returns `true` if this value is a prime number.
 *
 * @see IntMath.isPrime
 */
val Int.isPrime
    get() = IntMath.isPrime(this)
