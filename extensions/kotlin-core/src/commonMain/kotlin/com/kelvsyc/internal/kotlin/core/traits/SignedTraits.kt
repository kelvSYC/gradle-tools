package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Signed
import kotlin.math.absoluteValue

object ByteSigned : Signed<Byte> {
    override fun isPositive(value: Byte): Boolean = value > 0
    override fun isNegative(value: Byte): Boolean = value < 0
    override fun negate(value: Byte): Byte = (-value).toByte()
    override fun absoluteValue(value: Byte): Byte = value.toInt().absoluteValue.toByte()
}

object ShortSigned : Signed<Short> {
    override fun isPositive(value: Short): Boolean = value > 0
    override fun isNegative(value: Short): Boolean = value < 0
    override fun negate(value: Short): Short = (-value).toShort()
    override fun absoluteValue(value: Short): Short = value.toInt().absoluteValue.toShort()
}

object IntSigned : Signed<Int> {
    override fun isPositive(value: Int): Boolean = value > 0
    override fun isNegative(value: Int): Boolean = value < 0
    override fun negate(value: Int): Int = -value
    override fun absoluteValue(value: Int): Int = value.absoluteValue
}

object LongSigned: Signed<Long> {
    override fun isPositive(value: Long): Boolean = value > 0L
    override fun isNegative(value: Long): Boolean = value < 0L
    override fun negate(value: Long): Long = -value
    override fun absoluteValue(value: Long): Long = value.absoluteValue
}

object FloatSigned : Signed<Float> {
    override fun isPositive(value: Float): Boolean = value > 0.0f
    override fun isNegative(value: Float): Boolean = value < 0.0f
    override fun negate(value: Float): Float = -value
    override fun absoluteValue(value: Float): Float = value.absoluteValue
}

object DoubleSigned : Signed<Double> {
    override fun isPositive(value: Double): Boolean = value > 0.0
    override fun isNegative(value: Double): Boolean = value < 0.0
    override fun negate(value: Double): Double = -value
    override fun absoluteValue(value: Double): Double = value.absoluteValue
}
