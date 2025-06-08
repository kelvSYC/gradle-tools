package com.kelvsyc.kotlin.guava.math

import com.google.common.math.DoubleMath

/**
 * [Comparator] object that compares [Double]s using a fixed tolerance for nearly-equal values.
 *
 * @see DoubleMath.fuzzyCompare
 */
class FuzzyDoubleComparator(private val tolerance: Double) : Comparator<Double> {
    /**
     * Compares two values "fuzzily", with a tolerance for nearly-equal values
     *
     * @see DoubleMath.fuzzyCompare
     */
    override fun compare(o1: Double, o2: Double): Int = DoubleMath.fuzzyCompare(o1, o2, tolerance)
}
