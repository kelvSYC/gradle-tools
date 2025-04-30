package com.kelvsyc.kotlin.guava.math

import com.google.common.math.BigIntegerMath
import java.math.BigInteger
import java.math.RoundingMode

/**
 * Returns the base 10 logarithm of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see BigIntegerMath.log10
 */
fun BigInteger.log10(mode: RoundingMode) = BigIntegerMath.log10(this, mode)

/**
 * Returns the base 2 logarithm of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see BigIntegerMath.log2
 */
fun BigInteger.log2(mode: RoundingMode) = BigIntegerMath.log2(this, mode)

/**
 * Returns this value, rounded to a [Double] with the specified rounding mode.
 *
 * @see BigIntegerMath.roundToDouble
 */
fun BigInteger.roundToDouble(mode: RoundingMode) = BigIntegerMath.roundToDouble(this, mode)

/**
 * Returns the square root of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see BigIntegerMath.sqrt
 */
fun BigInteger.sqrt(mode: RoundingMode) = BigIntegerMath.sqrt(this, mode)

/**
 * Returns the smallest power of two greater than or equal to this value.
 *
 * @see BigIntegerMath.ceilingPowerOfTwo
 */
val BigInteger.ceilingPowerOfTwo
    get() = BigIntegerMath.ceilingPowerOfTwo(this)

/**
 * Returns the largest power of two less than or equal to this value.
 *
 * @see BigIntegerMath.floorPowerOfTwo
 */
val BigInteger.floorPowerOfTwo
    get() = BigIntegerMath.floorPowerOfTwo(this)

/**
 * Returns `true` if this value represents a power of two.
 *
 * @see BigIntegerMath.isPowerOfTwo
 */
val BigInteger.isPowerOfTwo
    get() = BigIntegerMath.isPowerOfTwo(this)
