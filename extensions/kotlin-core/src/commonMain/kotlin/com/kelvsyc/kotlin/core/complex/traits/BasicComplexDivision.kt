package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic
import com.kelvsyc.kotlin.core.traits.FusedMultiplyAdd

/**
 * Implementation of [ComplexDivision] that divides complex numbers according to their components, using Smith's formula
 * for dividing by a [Complex] divisor.
 *
 * For the cases where the divisor is a [Complex], the availability of a [FusedMultiplyAdd] may provide extra accuracy
 * due to fewer operations being performed.
 */
class BasicComplexDivision<T, I : Imaginary<T>, C : Complex<T>>(
    private val baseTraits: FloatingPoint<T>,
    private val arithmetic: FloatingPointArithmetic<T>,
    private val fma: FusedMultiplyAdd<T>? = null,
    private val comparator: Comparator<T>,
    private val imaginary: Imaginary.Factory<T, I>,
    private val complex: Complex.Factory<T, C>
) : ComplexDivision<T, I, C>,
    ImaginaryDivision<T, I> by BasicImaginaryDivision(baseTraits, arithmetic, imaginary),
    ComplexOnlyDivision<T, C> by BasicComplexOnlyDivision(baseTraits, arithmetic, fma, comparator, complex) {
    override fun divide(lhs: I, rhs: C): C {
        if (comparator.compare(baseTraits.absoluteValue(rhs.real), baseTraits.absoluteValue(rhs.imaginary)) > 0) {
            val p = arithmetic.divide(rhs.imaginary, rhs.real)
            val q = if (fma != null) {
                fma.fma(rhs.imaginary, p, rhs.real)
            } else {
                arithmetic.add(rhs.real, arithmetic.multiply(rhs.imaginary, p))
            }
            val ru = arithmetic.multiply(lhs.value, p)
            val su =  lhs.value
            return complex.ofCartesian(arithmetic.divide(ru, q), arithmetic.divide(su, q))
        } else {
            val p = arithmetic.divide(rhs.real, rhs.imaginary)
            val q = if (fma != null) {
                fma.fma(rhs.real, p, rhs.imaginary)
            } else {
                arithmetic.add(arithmetic.multiply(rhs.real, p), rhs.imaginary)
            }
            val ru = lhs.value
            val su = arithmetic.multiply(lhs.value, p)
            return complex.ofCartesian(arithmetic.divide(ru, q), arithmetic.divide(su, q))
        }
    }

    override fun divide(lhs: C, rhs: I): C = complex.ofCartesian(
        arithmetic.divide(lhs.imaginary, rhs.value),
        baseTraits.negate(arithmetic.divide(lhs.real, rhs.value))
    )
}
