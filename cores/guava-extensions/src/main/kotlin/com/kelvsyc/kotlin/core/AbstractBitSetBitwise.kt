package com.kelvsyc.kotlin.core

import com.google.common.base.Converter
import com.google.common.math.IntMath
import java.util.*

/**
 * Implementation of [Bitwise] for types that can be converted into a fixed-size [BitSet] that represents its structure.
 */
abstract class AbstractBitSetBitwise<T : Any> : AbstractConverterBasedBitwise<T, BitSet>() {
    /**
     * The number of bits that the converted [BitSet] takes up.
     */
    abstract val sizeBits: Int

    /**
     * [Converter] instance that converts objects of the specified type to the [BitSet] equivalent.
     *
     * The resulting [BitSet] from a conversion must be considered mutable; bitwise operations will mutate this bitset
     * before being converted back into the original object type.
     */
    abstract override val converter: Converter<T, BitSet>

    final override fun doAnd(lhs: BitSet, rhs: BitSet): BitSet = lhs.also { it.and(rhs) }
    final override fun doOr(lhs: BitSet, rhs: BitSet): BitSet = lhs.also { it.or(rhs) }
    final override fun doXor(lhs: BitSet, rhs: BitSet): BitSet = lhs.also { it.xor(rhs) }
    final override fun doInv(value: BitSet): BitSet = value.also { it.flip(0, sizeBits) }

    final override fun doLeftShift(value: BitSet, bitCount: Int): BitSet = value.also {
        for (i in bitCount ..< sizeBits) {
            it[i] = it[i - bitCount]
        }
        it.clear(0, bitCount)
    }
    final override fun doArithmeticRightShift(value: BitSet, bitCount: Int): BitSet = value.also {
        val signed = it[sizeBits - 1]
        for (i in 0 ..< sizeBits - bitCount) {
            it[i] = it[i + bitCount]
        }
        it.set(sizeBits - bitCount, sizeBits, signed)
    }
    final override fun doLogicalRightShift(value: BitSet, bitCount: Int): BitSet = value.also {
        for (i in 0 ..< sizeBits - bitCount) {
            it[i] = it[i + bitCount]
        }
        it.clear(sizeBits - bitCount, sizeBits)
    }

    final override fun doRotateLeft(value: BitSet, bitCount: Int): BitSet = value.also { result ->
        val n = bitCount % sizeBits
        val d = IntMath.gcd(sizeBits, n)
        for (i in 0 ..< d) {
            val tempBit = result[i]
            // positions is the sequence (i, (i + n) % sizeBits, (i + 2n) % sizeBits, ...), but in reverse order
            // Thus, we start with i, (i - n) + sizebits, and so on, with the last element being (i + n) % sizeBits
            val positions = generateSequence(i, nextFunction = {
                val next = (it - n).let { if (it < 0) it + sizeBits else it }
                next.takeIf { it != i }
            })
            positions.zipWithNext().forEach { (srcIndex, dstIndex) ->
                // This loop starts with replacing result[i], and then replacing the value where it was moved from
                // (ie. (i - n) + sizebits), and so on. The last iteration covered by this loop is
                // result[(i + 2n) % sizeBits] = result[(i + n) % sizeBits]
                result[dstIndex] = result[srcIndex]
            }
            // Close the loop
            result[i + n] = tempBit
        }
    }
    final override fun doRotateRight(value: BitSet, bitCount: Int): BitSet {
        // In-place rotation algorithm can accommodate negative rotations, so we do a negative left rotation.
        return doRotateLeft(value, -bitCount)
    }
}
