package com.kelvsyc.kotlin.core.traits

import java.math.BigInteger

/**
 * Implementation of [BitShift] for types that can be represented as a fixed-size bit collection, represented by a
 * [BigInteger].
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BigIntegerBitShift(private val sized: Sized<BigInteger>) : BitShift<BigInteger>,
    LeftShift<BigInteger> by BigIntegerLeftShift(sized),
    RightShift<BigInteger> by BigIntegerRightShift(sized),
    ArithmeticRightShift<BigInteger> by BigIntegerArithmeticRightShift(sized)
