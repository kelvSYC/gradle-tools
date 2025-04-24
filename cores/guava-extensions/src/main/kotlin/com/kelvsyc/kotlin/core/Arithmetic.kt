package com.kelvsyc.kotlin.core

/**
 * Interface denoting that a type supports arithmetic operations.
 *
 * Note that this interface does not impose the standard operator overloads (eg. `plus`, `minus`) on the type.
 *
 * @param T The type supporting bitwise operations.
 */
interface Arithmetic<T> {
    object FloatArithmetic : Arithmetic<Float> {
        override fun add(lhs: Float, rhs: Float): Float = lhs + rhs
        override fun subtract(lhs: Float, rhs: Float): Float = lhs - rhs
        override fun multiply(lhs: Float, rhs: Float): Float = lhs * rhs
        override fun divide(lhs: Float, rhs: Float): Float = lhs / rhs

        override fun negate(value: Float): Float = -value
    }
    object DoubleArithmetic : Arithmetic<Double> {
        override fun add(lhs: Double, rhs: Double): Double = lhs + rhs
        override fun subtract(lhs: Double, rhs: Double): Double = lhs - rhs
        override fun multiply(lhs: Double, rhs: Double): Double = lhs * rhs
        override fun divide(lhs: Double, rhs: Double): Double = lhs / rhs

        override fun negate(value: Double): Double = -value
    }

    fun add(lhs: T, rhs: T): T
    fun subtract(lhs: T, rhs: T): T
    fun multiply(lhs: T, rhs: T): T
    fun divide(lhs: T, rhs: T): T

    fun negate(value: T): T
}
