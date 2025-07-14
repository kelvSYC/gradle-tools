package com.kelvsyc.kotlin.core

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.math.max

/**
 * Implementation of [Bitwise] on [ByteArray] where the result of all operations would be done byte-by-byte on its
 * contents. All binary operations return new byte arrays the length of the longer of the two inputs.
 */
object ByteArrayBitwise : Bitwise<ByteArray> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun and(lhs: ByteArray, rhs: ByteArray): ByteArray {
        val result = ByteArray(max(lhs.size, rhs.size))
        for (i in 0 ..< result.size) {
            result[i] = if (i < lhs.size && i < rhs.size) {
                lhs[i] and rhs[i]
            } else {
                0
            }
        }
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun or(lhs: ByteArray, rhs: ByteArray): ByteArray {
        val result = ByteArray(max(lhs.size, rhs.size))
        for (i in 0 ..< result.size) {
            result[i] = if (i < lhs.size && i < rhs.size) {
                lhs[i] or rhs[i]
            } else if (i < lhs.size) {
                lhs[i]
            } else if (i < rhs.size) {
                rhs[i]
            } else {
                // This should normally be unreachable, but just in case...
                0
            }
        }
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun xor(lhs: ByteArray, rhs: ByteArray): ByteArray {
        val result = ByteArray(max(lhs.size, rhs.size))
        for (i in 0 ..< result.size) {
            result[i] = if (i < lhs.size && i < rhs.size) {
                lhs[i] xor rhs[i]
            } else if (i < lhs.size) {
                lhs[i]
            } else if (i < rhs.size) {
                rhs[i]
            } else {
                // This should normally be unreachable, but just in case...
                0
            }
        }
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun inv(value: ByteArray): ByteArray {
        val result = ByteArray(value.size)
        for (i in 0 ..< value.size) {
            result[i] = value[i].inv()
        }
        return result
    }
}
