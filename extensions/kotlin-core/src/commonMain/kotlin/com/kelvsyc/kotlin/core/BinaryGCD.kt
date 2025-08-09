package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.BitStore
import com.kelvsyc.kotlin.core.traits.GreatestCommonDivisor
import com.kelvsyc.kotlin.core.traits.IntegerArithmetic
import com.kelvsyc.kotlin.core.traits.IntegralConstants
import com.kelvsyc.kotlin.core.traits.Sized
import kotlin.math.min

/**
 * Class implementing the greatest common divisor for a generic integral type using the Binary GCD algorithm.
 *
 * This implementation avoids the use of branching (`if`-`else`) operations by using bitwise and arthmetic operations,
 * though depending on the implementations of those, they may be unavoidable. As such, this should be used as a default
 * implementation of last resort.
 */
class BinaryGCD<T>(
    private val sized: Sized,
    private val constants: IntegralConstants<T>,
    private val arithmetic: IntegerArithmetic<T>,
    private val bitStore: BitStore<T>,
    private val comparator: Comparator<T>
) : GreatestCommonDivisor<T> {
    @Suppress("detekt:ReturnCount")
    override fun gcd(lhs: T, rhs: T): T {
        require(comparator.compare(lhs, constants.zero) >= 0) { "Left operand to BinaryGCD must be non-negative" }
        require(comparator.compare(rhs, constants.zero) >= 0) { "Right operand to BinaryGCD must be non-negative" }

        // If one arg is zero, return the other one.
        if (constants.isZero(lhs)) return rhs
        if (constants.isZero(rhs)) return lhs

        val lhsTwos = bitStore.countTrailingZeroBits(lhs)
        val rhsTwos = bitStore.countTrailingZeroBits(rhs)

        var a = bitStore.rightShift(lhs, lhsTwos)
        var b = bitStore.rightShift(rhs, rhsTwos)
        while (comparator.compare(a, b) != 0) {
            // gcd(a, b) = gcd(a - b, b), if a > b
            val delta = arithmetic.subtract(a, b) // delta is even as a and b are both odd

            // This is min(delta, constants.zero), but using bitwise operations to avoid branching
            // See http://graphics.stanford.edu/~seander/bithacks.html#IntegerMinOrMax
            // bitStore.arithmeticRightShift(delta, sized.sizeBits - 1) == constants.allClear if delta >= 0
            // bitStore.arithmeticRightShift(delta, sized.sizeBits - 1) == constants.allSet if delta < 0
            val minDelta = bitStore.and(delta, bitStore.arithmeticRightShift(delta, sized.sizeBits - 1))

            // This is a = abs(a - b), but using arithmetic operations to avoid branching
            // If delta >= 0, then minDelta == 0, and a = delta
            // If delta < 0, then minDelta == delta, and thus a = -delta
            // This makes it so that a is now non-negative and even (as delta is even)
            a = arithmetic.subtract(arithmetic.subtract(delta, minDelta), minDelta)
            b = arithmetic.add(b, minDelta) // b is the smaller of "old a" or b

            // a is now even, b is now odd, therefore we can shift off the trailing zeros in a
            a = bitStore.rightShift(a, bitStore.countTrailingZeroBits(a))
        }
        return bitStore.leftShift(a, min(lhsTwos, rhsTwos))
    }
}
