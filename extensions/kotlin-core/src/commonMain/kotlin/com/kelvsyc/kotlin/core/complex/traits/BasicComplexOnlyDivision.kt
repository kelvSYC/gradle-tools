package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic
import com.kelvsyc.kotlin.core.traits.FusedMultiplyAdd

/**
 * Basic implementation of [ComplexOnlyDivision] for [Complex] types, using Smith's formula for dividing by a [Complex]
 * divisor.
 *
 * For the cases where the divisor is a [Complex], the availability of a [FusedMultiplyAdd] may provide extra accuracy
 * due to fewer operations being performed.
 */
class BasicComplexOnlyDivision<T, C : Complex<T>>(
    private val baseTraits: FloatingPoint<T>,
    private val arithmetic: FloatingPointArithmetic<T>,
    private val fma: FusedMultiplyAdd<T>? = null,
    private val comparator: Comparator<T>,
    private val factory: Complex.Factory<T, C>
) : ComplexOnlyDivision<T, C> {
    override fun divide(lhs: T, rhs: T): T = arithmetic.divide(lhs, rhs)

    override fun divide(lhs: C, rhs: C): C {
        if (comparator.compare(baseTraits.absoluteValue(rhs.real), baseTraits.absoluteValue(rhs.imaginary)) > 0) {
            val p = arithmetic.divide(rhs.imaginary, rhs.real)
            val q = if (fma != null) {
                fma.fma(rhs.imaginary, p, rhs.real)
            } else {
                arithmetic.add(rhs.real, arithmetic.multiply(rhs.imaginary, p))
            }
            val ru = if (fma != null) {
                fma.fma(lhs.imaginary, p, lhs.real)
            } else {
                arithmetic.add(lhs.real, arithmetic.multiply(lhs.imaginary, p))
            }
            val su =  if (fma != null) {
                baseTraits.negate(fma.fma(lhs.real, p, baseTraits.negate(lhs.imaginary)))
            } else {
                arithmetic.subtract(lhs.imaginary, arithmetic.multiply(lhs.real, p))
            }
            return factory.ofCartesian(arithmetic.divide(ru, q), arithmetic.divide(su, q))
        } else {
            val p = arithmetic.divide(rhs.real, rhs.imaginary)
            val q = if (fma != null) {
                fma.fma(rhs.real, p, rhs.imaginary)
            } else {
                arithmetic.add(arithmetic.multiply(rhs.real, p), rhs.imaginary)
            }
            val ru = if (fma != null) {
                fma.fma(lhs.real, p, lhs.imaginary)
            } else {
                arithmetic.add(arithmetic.multiply(lhs.real, p), lhs.imaginary)
            }
            val su = if (fma != null) {
                fma.fma(lhs.imaginary, p, baseTraits.negate(lhs.real))
            } else {
                arithmetic.subtract(arithmetic.multiply(lhs.imaginary, p), lhs.real)
            }
            return factory.ofCartesian(arithmetic.divide(ru, q), arithmetic.divide(su, q))
        }
    }

    override fun divide(lhs: T, rhs: C): C {
        if (comparator.compare(baseTraits.absoluteValue(rhs.real), baseTraits.absoluteValue(rhs.imaginary)) > 0) {
            val p = arithmetic.divide(rhs.imaginary, rhs.real)
            val q = if (fma != null) {
                fma.fma(rhs.imaginary, p, rhs.real)
            } else {
                arithmetic.add(rhs.real, arithmetic.multiply(rhs.imaginary, p))
            }
            val ru = lhs
            val su =  baseTraits.negate(arithmetic.multiply(lhs, p))
            return factory.ofCartesian(arithmetic.divide(ru, q), arithmetic.divide(su, q))
        } else {
            val p = arithmetic.divide(rhs.real, rhs.imaginary)
            val q = if (fma != null) {
                fma.fma(rhs.real, p, rhs.imaginary)
            } else {
                arithmetic.add(arithmetic.multiply(rhs.real, p), rhs.imaginary)
            }
            val ru = arithmetic.multiply(lhs, p)
            val su = baseTraits.negate(lhs)
            return factory.ofCartesian(arithmetic.divide(ru, q), arithmetic.divide(su, q))
        }
    }

    override fun divide(lhs: C, rhs: T): C =
        factory.ofCartesian(arithmetic.divide(lhs.real, rhs), arithmetic.divide(lhs.imaginary, rhs))
}
