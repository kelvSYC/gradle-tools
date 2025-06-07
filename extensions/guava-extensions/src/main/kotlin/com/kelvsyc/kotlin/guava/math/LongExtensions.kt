package com.kelvsyc.kotlin.guava.math

import com.google.common.math.LongMath
import java.math.RoundingMode

/**
 * Returns the base 10 logarithm of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see LongMath.log10
 */
fun Long.log10(mode: RoundingMode) = LongMath.log10(this, mode)

/**
 * Returns the base 2 logarithm of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see LongMath.log2
 */
fun Long.log2(mode: RoundingMode) = LongMath.log2(this, mode)

/**
 * Returns this value, rounded to a [Double] with the specified rounding mode.
 *
 * @see LongMath.roundToDouble
 */
fun Long.roundToDouble(mode: RoundingMode) = LongMath.roundToDouble(this, mode)

/**
 * Returns the square root of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see LongMath.sqrt
 */
fun Long.sqrt(mode: RoundingMode) = LongMath.sqrt(this, mode)

/**
 * Returns the smallest power of two greater than or equal to this value.
 *
 * @see LongMath.ceilingPowerOfTwo
 */
val Long.ceilingPowerOfTwo
    get() = LongMath.ceilingPowerOfTwo(this)

/**
 * Returns the largest power of two less than or equal to this value.
 *
 * @see LongMath.floorPowerOfTwo
 */
val Long.floorPowerOfTwo
    get() = LongMath.ceilingPowerOfTwo(this)

/**
 * Returns `true` if this value represents a power of two.
 *
 * @see LongMath.isPowerOfTwo
 */
val Long.isPowerOfTwo
    get() = LongMath.isPowerOfTwo(this)

/**
 * Returns `true` if this value is a prime number.
 *
 * @see LongMath.isPrime
 */
val Long.isPrime
    get() = LongMath.isPrime(this)
