package com.kelvsyc.kotlin.core.fp

import com.kelvsyc.kotlin.core.traits.Signed as BaseSigned

/**
 * A `DoubleFloatingPoint` is a number that can be expressed as the sum of two floating-point numbers of an established
 * floating-point type. This is generally used to extend the precision of a floating-point type by having the two
 * numbers be "non-overlapping". This is where the least significant bit of one number is more significant than the most
 * significant bit of the other number.
 *
 * Note that the representation of a value need not be unique. Specifically, through the use of the sign bits, any
 * floating-point value can be split into two non-overlapping and "non-adjacent" components. (Here, two numbers are
 * considered adjacent if they overlap or one value doubled overlaps with the other.) One advantage of breaking down a
 * value into two (or more) components is the ability to perform certain floating-point operations exactly in a more
 * efficient manner. In this sense, the smaller of the two numbers can be thought of as an error term to the larger
 * number.
 *
 * @param F The underlying floating-point type.
 */
interface DoubleFloatingPoint<F> {
    /**
     * Interface extending [Signed][BaseSigned], used for signed operations for [DoubleFloatingPoint] instances, derived
     * from the implementation of [Signed][BaseSigned] used for the underlying floating-point type.
     *
     * @param base The object providing signed operations on the underlying type.
     *
     * @param D The doubled floating-point type
     * @param F The underlying floating-point type
     */
    interface Signed<F, D : DoubleFloatingPoint<F>> : BaseSigned<D> {
        val base: BaseSigned<F>
    }

    /**
     * Returns the larger of the two values.
     */
    val high: F

    /**
     * Returns the smaller of the two values.
     */
    val low: F

    /**
     * Returns the value as expressed as single instance of the underlying type.
     *
     * This operation may cause a loss of precision. Additionally, note that while the [high] and [low] values are
     * non-overlapping, this does not imply that the value returned from this function is equal to [high] alone.
     */
    fun toFloatingPoint(): F
}
