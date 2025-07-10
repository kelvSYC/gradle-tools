package com.kelvsyc.kotlin.core

import com.google.common.math.IntMath
import java.util.*

/**
 * Implementation of [BitRotate] for types that can be represented as a fixed-size [BitSet].
 *
 * Note that all operations treat its value as being mutable.
 */
abstract class AbstractBitSetBitRotate : BitRotate<BitSet> {
    /**
     * The size of the fixed-size [BitSet].
     */
    abstract val sizeBits: Int

    @OptIn(ExperimentalStdlibApi::class)
    override fun rotateLeft(value: BitSet, bitCount: Int): BitSet = value.also { result ->
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

    override fun rotateRight(value: BitSet, bitCount: Int): BitSet {
        // In-place rotation algorithm can accommodate negative rotations, so we do a negative left rotation.
        return rotateLeft(value, -bitCount)
    }
}
