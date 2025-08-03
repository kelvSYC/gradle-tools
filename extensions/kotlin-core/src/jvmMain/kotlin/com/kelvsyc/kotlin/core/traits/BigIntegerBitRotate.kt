package com.kelvsyc.kotlin.core.traits

import java.math.BigInteger

/**
 * Implementation of [BitRotate] for types that can be represented as a fixed-size bit collection, represented by a
 * [BigInteger].
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BigIntegerBitRotate(sized: Sized<BigInteger>) :
    BitRotate<BigInteger> by SyntheticBitRotate<BigInteger>(sized,BigIntegerBitShift(sized), BigIntegerBitwise(sized))
