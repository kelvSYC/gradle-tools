package com.kelvsyc.kotlin.guava.math

import com.google.common.math.DoubleMath
import java.math.RoundingMode

/**
 * Returns the base 2 logarithm of this value, rounded with the specified rounding mode to an [Int].
 *
 * @see DoubleMath.log2
 */
fun Double.log2(mode: RoundingMode) = DoubleMath.log2(this, mode)

/**
 * Returns the [Int] value that is equal to this value, rounded with the specified rounding mode, if possible.
 *
 * @see DoubleMath.roundToInt
 */
fun Double.roundToInt(mode: RoundingMode) = DoubleMath.roundToInt(this, mode)

/**
 * Returns the [Long] value that is equal to this value, rounded with the specified rounding mode, if possible.
 *
 * @see DoubleMath.roundToLong
 */
fun Double.roundToLong(mode: RoundingMode) = DoubleMath.roundToLong(this, mode)

/**
 * Returns the [BigInteger][java.math.BigInteger] value that is equal to this value, rounded with the specified rounding
 * mode, if possible.
 */
fun Double.roundToBigInteger(mode: RoundingMode) = DoubleMath.roundToBigInteger(this, mode)

/**
 * Returns `true` if this value represents a mathematical integer.
 *
 * @see DoubleMath.isMathematicalInteger
 */
val Double.isMathematicalInteger
    get() = DoubleMath.isMathematicalInteger(this)

/**
 * Returns `true` if this value is exactly equal to `2^k` for some finite integer `k`.
 *
 * @see DoubleMath.isPowerOfTwo
 */
val Double.isPowerOfTwo
    get() = DoubleMath.isPowerOfTwo(this)

/**
 * Returns the base 2 logarithm of this value.
 *
 * @see DoubleMath.log2
 */
val Double.log2
    get() = DoubleMath.log2(this)
