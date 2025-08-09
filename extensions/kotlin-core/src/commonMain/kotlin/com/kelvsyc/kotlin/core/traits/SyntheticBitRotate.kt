package com.kelvsyc.kotlin.core.traits

/**
 * Implementation of [BitRotate] through a synthetic combination of a left shift and a right shift.
 *
 * The resulting [BitRotate] supports negative rotations, with a negative [rotateLeft] being equivalent to a positive
 * [rotateRight].
 *
 * @param sized Traits instance allowing query on the size of the type
 * @param baseShift Traits instance providing bit shifting operations on the type
 * @param baseBitwise Traits instance providing bitwise operations on the type
 * @param T The type for which bit rotate operations are to be defined
 */
class SyntheticBitRotate<T>(
    private val sized: Sized,
    private val baseShift: BitShift<T>,
    private val baseBitwise: Bitwise<T>
) : BitRotate<T> {
    @Suppress("detekt:ReturnCount")
    override fun rotateLeft(value: T, bitCount: Int): T {
        val trueBitCount = bitCount.rem(sized.sizeBits)
        if (trueBitCount == 0) {
            return value
        } else if (trueBitCount > 0) {
            val left = baseShift.leftShift(value, trueBitCount)
            val right = baseShift.rightShift(value, sized.sizeBits - trueBitCount)
            return baseBitwise.or(left, right)
        } else {
            val left = baseShift.leftShift(value, sized.sizeBits + trueBitCount)
            val right = baseShift.rightShift(value, -trueBitCount)
            return baseBitwise.or(left, right)
        }
    }

    override fun rotateRight(value: T, bitCount: Int): T = rotateLeft(value, -bitCount)
}
