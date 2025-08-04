package com.kelvsyc.kotlin.core.fp

import com.kelvsyc.kotlin.core.traits.Signed

/**
 * Implementation of [Signed] for [DoubleFloatingPoint], derived from the implementation of [Signed] used for the
 * underlying floating-point type.
 *
 * @param base The object providing signed operations on the underlying type.
 *
 * @param D The doubled floating-point type
 * @param F The underlying floating-point type
 */
abstract class AbstractDoubleFloatingPointSigned<F, D : DoubleFloatingPoint<F>>(
    override val base: Signed<F>
) : DoubleFloatingPoint.Signed<F, D> {
    protected abstract fun create(high: F, low: F): D

    override fun isNegative(value: D): Boolean = base.isNegative(value.high)
    override fun isPositive(value: D): Boolean = base.isPositive(value.high)
    override fun negate(value: D): D =
        create(base.negate(value.high), base.negate(value.low))
    override fun absoluteValue(value: D): D =
        create(base.absoluteValue(value.high), base.absoluteValue(value.low))
}
