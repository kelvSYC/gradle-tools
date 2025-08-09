package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.AbstractBinary32Traits
import com.kelvsyc.kotlin.core.traits.AbstractBinary64Traits

object FloatTraits : AbstractBinary32Traits<Float>(FloatSigned) {
    override val zero: Float = 0.0f
    override val one: Float = 1.0f
    override val positiveInfinity: Float = Float.POSITIVE_INFINITY
    override val negativeInfinity: Float = Float.NEGATIVE_INFINITY
    override val NaN: Float = Float.NaN

    override fun isNaN(value: Float): Boolean = value.isNaN()
    override fun isFinite(value: Float): Boolean = value.isFinite()
    override fun isInfinite(value: Float): Boolean = value.isInfinite()
}

object DoubleTraits : AbstractBinary64Traits<Double>(DoubleSigned) {
    override val zero: Double = 0.0
    override val one: Double = 1.0
    override val positiveInfinity: Double = Double.POSITIVE_INFINITY
    override val negativeInfinity: Double = Double.NEGATIVE_INFINITY
    override val NaN: Double = Double.NaN

    override fun isNaN(value: Double): Boolean = value.isNaN()
    override fun isFinite(value: Double): Boolean = value.isFinite()
    override fun isInfinite(value: Double): Boolean = value.isInfinite()
}
