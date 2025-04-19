package com.kelvsyc.kotlin.core

import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.safeCast

/**
 * Abstract base class for a floating point number represented as two instances of a floating point number with half
 * of the precision. Generally used to approximate a floating point value with double the precison (eg. a
 * "double-[Float]" being used to approximate a [Double]).
 *
 * The way that the number is represented is through two values called [value], representing the main part of the
 * number, and [error], a smaller number that extends the precision of the combined number; the number represented is
 * nominally the sum of the two values.
 *
 * This class implements the arithmetic operators on the type, for both `T op T` and `T op F` varieties.
 * No extension implementation is provided for `F op T` as part of this class.
 *
 * @param T a self-type
 * @param F the underlying floating-point number type
 */
@Suppress("detekt:TooManyFunctions")
abstract class AbstractDoubleFloatingPoint<T : AbstractDoubleFloatingPoint<T, F>, F : Any> protected constructor(protected val value: F, protected val error: F) : Comparable<T> {
    // The implementation of this class is based on the C++ robust-predicate library
    // https://github.com/dengwirda/robust-predicate
    // This is in turn based on "Adaptive Precision Floating-Point Arithmetic and Fast Robust Geometric Predicates"
    // https://link.springer.com/article/10.1007/PL00009321

    abstract class AbstractCompanion<T : AbstractDoubleFloatingPoint<T, F>, F : Any> {
        fun create(value: F): T = create(value, zero)
        abstract fun create(value: F, error: F): T

        abstract val doubledType: KClass<T>
        abstract val underlyingType: KClass<F>

        /**
         * The precision of the underlying floating point type.
         */
        abstract val precision: Int

        /**
         * The split point of the floating point type, defined as half of the precision, rounded up.
         *
         * When multiplying two double floating point numbers, it may be necessary to split a value into two
         * nonoverlapping values, each containing half of the bits of the original number (rounded down); any odd bit
         * out (as in the case of [Double], whose precision is odd) is encoded in the sign bit of the second number.
         */
        val splitPoint = Math.ceilDiv(precision, 2)

        /**
         * The `splitter` is a value representing `2^s + 1`, where `s` is defined in [splitPoint].
         */
        abstract val splitter: F

        abstract val zero: F

        /**
         * [Comparator] representing the natural order of the underlying floating-point number type.
         */
        abstract val baseComparator: Comparator<F>

        /**
         * [Comparator] representing the natural order of the floating-point number type.
         */
        val comparator: Comparator<T> = compareBy<T, F>(baseComparator) { it.value }.thenBy(baseComparator) { it.error }

        /**
         * A function that performs the "fused multiply add" (or FMA) algorithm on three arguments: multiplying the
         * first two and adding the third to the result, in a manner that performs a rounding operation only once.
         *
         * For some types, such a method may exist (see: [Math.fma]), while other types may require emulation.
         *
         * @return The FMA function, or `null` if it does not exist.
         */
        abstract val fma: ((F, F, F) -> F)?
    }

    abstract class AbstractArithmetic<T : AbstractDoubleFloatingPoint<T, F>, F : Any>: Arithmetic<T> {
        override fun add(lhs: T, rhs: T): T = lhs + rhs
        override fun subtract(lhs: T, rhs: T): T = lhs - rhs
        override fun multiply(lhs: T, rhs: T): T = lhs * rhs
        override fun divide(lhs: T, rhs: T): T = lhs / rhs
        override fun negate(value: T): T = -value
    }

    protected abstract val traits: AbstractCompanion<T, F>
    protected abstract val arithmetic: Arithmetic<F>

    // Implementation of the FAST-TWO-SUM algorithm as outlined in Shewchuk
    // Adds two numbers and expresses it in the form of a sum and error pair.
    // Precondition: abs(a) >= abs(b)
    protected fun fastTwoSum(a: F, b: F): Pair<F, F> {
        val s = arithmetic.add(a, b)
        val bVirt = arithmetic.subtract(s, a)
        val e = arithmetic.subtract(b, bVirt)
        return s to e
    }
    protected fun fastTwoDiff(a: F, b: F): Pair<F, F> {
        val s = arithmetic.subtract(a, b)
        val bVirt = arithmetic.subtract(a, s)
        val e = arithmetic.subtract(bVirt, b)
        return s to e
    }

    // Implementation of the TWO-SUM algorithm as outlined in Shewchuk
    // Adds two numbers and expresses it in the form of a sum and error pair.
    protected fun twoSum(a: F, b: F): Pair<F, F> {
        val s = arithmetic.add(a, b)
        val bVirt = arithmetic.subtract(s, a)
        val aVirt = arithmetic.subtract(s, bVirt)
        val bRound = arithmetic.subtract(b, bVirt)
        val aRound = arithmetic.subtract(a, aVirt)
        val e = arithmetic.add(aRound, bRound)
        return s to e
    }
    protected fun twoDiff(a: F, b: F): Pair<F, F> {
        val s = arithmetic.subtract(a, b)
        val bVirt = arithmetic.subtract(a, s)
        val aVirt = arithmetic.add(s, bVirt)
        val bRound = arithmetic.subtract(bVirt, b)
        val aRound = arithmetic.subtract(a, aVirt)
        val e = arithmetic.add(aRound, bRound)
        return s to e
    }

    protected fun twoSum(aH: F, al: F, b: F): Pair<F, F> {
        val (t1, t0) = twoSum(aH, b)
        val newT0 = arithmetic.add(t0, al)
        return fastTwoSum(t1, newT0)
    }
    protected fun twoDiff(aH: F, aL: F, b: F): Pair<F, F> {
        val (t1, t0) = twoDiff(aH, b)
        val newT0 = arithmetic.add(t0, aL)
        return fastTwoSum(t1, newT0)
    }

    protected fun twoDiff(aH: F, aL: F, bH: F, bL: F): Pair<F, F> {
        val (s1, s0) = twoDiff(aH, bH)
        val (t1, t0) = twoDiff(aL, bL)
        val (w1, w0) = fastTwoSum(s1, arithmetic.add(s0, t1))
        return w1 to arithmetic.add(w0, t0)
    }

    // Implementation of the SPLIT algorithm as outlined in Shewchuk
    // Splits a floating point number into two, where the significand of the first is evenly split among the two outputs
    protected fun split(a: F): Pair<F, F> {
        val c = arithmetic.multiply(a, traits.splitter)
        val b = arithmetic.subtract(c, a)
        val s = arithmetic.subtract(c, b)
        return s to arithmetic.subtract(a, s)
    }

    // Implementation of the TWO-PROD algorithm as outlined in Shewchuk
    // Multiplies two floating point numbers together, and expresses it in the form of a product and error pair
    protected fun twoProd(a: F, b: F): Pair<F, F> {
        val s = arithmetic.multiply(a, b)
        // Note that there are two algorithms, depending on whether F supports fma
        return if (traits.fma != null) {
            s to traits.fma!!(a, b, arithmetic.negate(s))
        } else {
            val (ah, al) = split(a)
            val (bh, bl) = split(b)

            val e1 = arithmetic.subtract(s, arithmetic.multiply(ah, bh))
            val e2 = arithmetic.subtract(e1, arithmetic.multiply(al, bh))
            val e3 = arithmetic.subtract(e2, arithmetic.subtract(ah, bl))
            s to arithmetic.subtract(arithmetic.multiply(al, bl), e3)
        }
    }
    protected fun twoProd(aH: F, aL: F, b: F): Pair<F, F> {
        val (t0, t1) = twoProd(aH, b)
        val newT0 = if (traits.fma != null) {
            traits.fma!!(aL, b, t0)
        } else {
            arithmetic.add(t0, arithmetic.multiply(aL, b))
        }
        return fastTwoSum(t1, newT0)
    }

    operator fun plus(rhs: T): T {
        val (s1, s0) = twoSum(value, rhs.value)
        val (t1, t0) = twoSum(error, rhs.error)
        val (w1, w0) = fastTwoSum(s1, arithmetic.add(s0, t1))
        val x = fastTwoSum(w1, arithmetic.add(w0, t0))
        return traits.create(x.first, x.second)
    }
    operator fun plus(rhs: F): T {
        val sum = twoSum(value, error, rhs)
        return traits.create(sum.first, sum.second)
    }

    operator fun minus(rhs: T): T {
        val result = twoDiff(value, error, rhs.value, rhs.error)
        return traits.create(result.first, result.second)
    }
    operator fun minus(rhs: F): T {
        val result = twoDiff(value, error, rhs)
        return traits.create(result.first, result.second)
    }

    operator fun times(rhs: T): T {
        val (t1, t0) = twoProd(value, rhs.value)
        val ss = if (traits.fma != null) {
            arithmetic.multiply(error, rhs.error).let {
                traits.fma!!(value, rhs.error, it)
            }.let {
                traits.fma!!(error, rhs.error, it)
            }
        } else {
            val s1 = arithmetic.multiply(error, rhs.error)
            val s2 = arithmetic.multiply(value, rhs.error)
            val s3 = arithmetic.multiply(error, rhs.value)
            arithmetic.add(s1, arithmetic.add(s2, s3))
        }
        val newT0 = arithmetic.add(t0, ss)
        val result = fastTwoSum(t1, newT0)
        return traits.create(result.first, result.second)
    }
    operator fun times(rhs: F): T {
        val result = twoProd(value, error, rhs)
        return traits.create(result.first, result.second)
    }

    operator fun div(rhs: T): T {
        val t1 = arithmetic.divide(value, rhs.value)
        val (r1, r0) = twoProd(rhs.value, rhs.error, t1)
        val (w1, w0) = twoDiff(value, error, r1, r0)

        val t0 = arithmetic.divide(w1, rhs.error)
        val (s1, s0) = twoProd(rhs.value, rhs.error, t0)
        val (u1, u0) = twoDiff(w1, w0, s1, s0)

        val ee = arithmetic.divide(u1, rhs.value)
        val (q1, q0) = fastTwoSum(t1, t0)
        val result = twoSum(q1, q0, ee)
        return traits.create(result.first, result.second)
    }
    operator fun div(rhs: F): T {
        val t1 = arithmetic.divide(value, rhs)
        val (p1, p0) = twoProd(t1, rhs)
        var dd = arithmetic.subtract(value, p1)
        dd = arithmetic.subtract(dd, p0)
        dd = arithmetic.add(dd, error)
        val t0 = arithmetic.divide(dd, rhs)
        val result = fastTwoSum(t1, t0)
        return traits.create(result.first, result.second)
    }

    operator fun unaryPlus(): T = traits.doubledType.cast(this)
    operator fun unaryMinus(): T = traits.create(arithmetic.negate(value), arithmetic.negate(error))

    override fun compareTo(other: T): Int = traits.comparator.compare(traits.doubledType.cast(this), other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        // We define equality only against this type and the underlying type
        val otherFloat = traits.doubledType.safeCast(other)
        if (otherFloat != null) return compareTo(otherFloat) == 0
        val otherUnderlying = traits.underlyingType.safeCast(other)
        if (otherUnderlying != null) return value == otherUnderlying

        return false
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + error.hashCode()
        return result
    }
}
