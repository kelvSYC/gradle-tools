package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.Rational

/**
 * Implementation of [Signed] for a [Rational] type, given an underlying type that is also [Signed].
 */
class RationalSigned<T, R : Rational<T>>(
    private val baseSigned: Signed<T>,
    private val factory: Rational.Factory<T, R>
) : Signed<R> {
    override fun isPositive(value: R): Boolean =
        (baseSigned.isPositive(value.numerator) && baseSigned.isPositive(value.denominator)) ||
                (baseSigned.isNegative(value.numerator) && baseSigned.isNegative(value.denominator))

    override fun isNegative(value: R): Boolean =
        (baseSigned.isNegative(value.numerator) && baseSigned.isPositive(value.denominator)) ||
                (baseSigned.isPositive(value.numerator) && baseSigned.isNegative(value.denominator))

    // FIXME if value.numerator == traits.minValue, then negating will overflow
    override fun negate(value: R): R = factory.create(baseSigned.negate(value.numerator), value.denominator)

    // FIXME if value.numerator == traits.minValue or value.denominator == traits.minValue, then absoluteValue will overflow
    override fun absoluteValue(value: R): R = factory.create(
        baseSigned.absoluteValue(value.numerator), baseSigned.absoluteValue(value.denominator)
    )
}
