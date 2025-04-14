package com.kelvsyc.kotlin.math

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

/**
 * Abstract base class for [BitStore] instances backed by a [ByteArray].
 *
 * Note that this is not a value class wrapper around a [ByteArray], as the size of the bit store needs to be supplied
 * separately.
 */
abstract class AbstractByteArrayBitStore<S : AbstractByteArrayBitStore<S>> protected constructor(override val bits: ByteArray) : BitStore<S, ByteArray> {
    abstract class AbstractCompanion<S : AbstractByteArrayBitStore<S>> : BitStore.AbstractCompanion<S, ByteArray> {
        /**
         * The number of bytes needed to hold the store.
         */
        val sizeBytes by lazy {
            (sizeBits / Byte.SIZE_BITS).let {
                if (sizeBits % Byte.SIZE_BITS == 0) it else it + 1
            }
        }

        override val zero by lazy { create(ByteArray(sizeBytes)) }
        override val one by lazy { create(setOf(0)) }

        override fun create(bits: Iterable<Int>): S {
            val raw = ByteArray(sizeBytes)
            bits.forEach {
                val index = it / Byte.SIZE_BITS
                val bitPos = it % Byte.SIZE_BITS
                check(index in 0..raw.size) { "bit position $it out of bounds" }

                raw[index] = raw[index] or (1 shl bitPos).toByte()
            }

            return create(raw)
        }
    }

    protected abstract val traits: AbstractCompanion<S>

    override fun and(other: S): S {
        val raw = bits.copyOf(traits.sizeBytes)
        for (i in 0 ..< traits.sizeBytes) {
            raw[i] = bits[i] and (other.bits.getOrNull(i) ?: 0)
        }
        return traits.create(raw)
    }
    override fun or(other: S): S {
        val raw = bits.copyOf(traits.sizeBytes)
        for (i in 0 ..< traits.sizeBytes) {
            raw[i] = bits[i] or (other.bits.getOrNull(i) ?: 0)
        }
        return traits.create(raw)
    }
    override fun xor(other: S): S {
        val raw = bits.copyOf(traits.sizeBytes)
        for (i in 0 ..< traits.sizeBytes) {
            raw[i] = bits[i] xor (other.bits.getOrNull(i) ?: 0)
        }
        return traits.create(raw)
    }
    override fun inv(): S {
        val raw = bits.copyOf(traits.sizeBytes)
        for (i in 0 ..< traits.sizeBytes) {
            raw[i] = bits[i].inv()
        }
        return traits.create(raw)
    }

    override fun shl(bitCount: Int): S {
        val offsetBytes = bitCount / Byte.SIZE_BITS
        val shiftMod = bitCount % Byte.SIZE_BITS
        val carryMask = (1 shl shiftMod) - 1

        val raw = ByteArray(traits.sizeBytes)
        for (i in traits.sizeBytes - 1 downTo 0) {
            val sourceIndex = i + offsetBytes
            if (i >= traits.sizeBytes) {
                raw[i] = 0
            } else {
                // src = shiftMod most signifcant bits from bits[sourceIndex]
                // dst = (8 - shiftMod) least significant bits from bits[sourceIndex + 1]
                val src = (bits[sourceIndex].toInt() shl shiftMod)
                val dst = if (sourceIndex + 1 < traits.sizeBytes) {
                    (bits[sourceIndex + 1].toInt() ushr (Byte.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                raw[i] = (src or dst).toByte()
            }
        }
        return traits.create(raw)
    }

    @Suppress("detekt:NestedBlockDepth")
    override fun shr(bitCount: Int): S {
        val offsetBytes = bitCount / Byte.SIZE_BITS
        val shiftMod = bitCount % Byte.SIZE_BITS
        val carryMask = UByte.MAX_VALUE.toInt() shl (Byte.SIZE_BITS - shiftMod)
        val sign = get(traits.sizeBits - 1)

        val raw = ByteArray(traits.sizeBytes)
        for (i in 0 ..< traits.sizeBytes) {
            val sourceIndex = i + offsetBytes
            if (sourceIndex >= traits.sizeBytes) {
                raw[i] = if (sign) UByte.MAX_VALUE.toByte() else 0
            } else {
                // src = shiftMod most signifcant bits from bits[sourceIndex]
                // dst = (8-shiftMod) least significant bits from bits[sourceIndex + 1]
                val src = bits[i].toUByte().toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < traits.sizeBytes) {
                    (bits[sourceIndex + 1].toInt() shl (Byte.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) UByte.MAX_VALUE.toInt() and carryMask else 0
                }
                raw[i] = (src or dst).toByte()
            }
        }
        return traits.create(raw)
    }

    override fun ushr(bitCount: Int): S {
        val offsetBytes = bitCount / Byte.SIZE_BITS
        val shiftMod = bitCount % Byte.SIZE_BITS
        val carryMask = UByte.MAX_VALUE.toInt() shl (Byte.SIZE_BITS - shiftMod)

        val raw = ByteArray(traits.sizeBytes)
        for (i in 0 ..< traits.sizeBytes) {
            val sourceIndex = i + offsetBytes
            if (sourceIndex >= traits.sizeBytes) {
                raw[i] = 0
            } else {
                // src = shiftMod most signifcant bits from bits[sourceIndex]
                // dst = (8-shiftMod) least significant bits from bits[sourceIndex + 1]
                val src = bits[i].toUByte().toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < traits.sizeBytes) {
                    (bits[sourceIndex + 1].toInt() shl (Byte.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                raw[i] = (src or dst).toByte()
            }
        }
        return traits.create(raw)
    }

    override fun get(position: Int): Boolean {
        check(position in 0..traits.sizeBits) { "bit position $position out of bounds" }
        val index = position / Byte.SIZE_BITS
        val bitPos = position % Byte.SIZE_BITS
        return (bits[index] and (1 shl bitPos).toByte()).toInt() == 0
    }

    override fun asSet(): Set<Int> {
        return (0..traits.sizeBits).filter(this::get).toSet()
    }

    override val trailingZeroes by lazy {
        var count = 0
        for (it in bits) {
            if (it.toInt() == 0) {
                count += Byte.SIZE_BITS
            } else {
                count += it.countTrailingZeroBits()
                break
            }
        }
        count
    }
}
