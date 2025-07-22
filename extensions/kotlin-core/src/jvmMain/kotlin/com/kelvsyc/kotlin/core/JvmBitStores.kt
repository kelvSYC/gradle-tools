package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.BitStore
import java.math.BigInteger
import java.util.*

/**
 * Functions relating to obtaining [BitStore] instances from JVM types
 */
object JvmBitStores {
    /**
     * Creates a [BitStore] instance for [BigInteger]s of a fixed size.
     */
    fun bigInteger(sizeBits: Int): BitStore<BigInteger> = object : BitStore<BigInteger>,
        BitCollection<BigInteger> by BigIntegerBitCollection(sizeBits),
        BitShift<BigInteger> by BigIntegerBitShift(sizeBits),
        Bitwise<BigInteger> by BigIntegerBitwise(sizeBits) {
        override val sizeBits: Int = sizeBits
    }

    /**
     * Creates a [BitStore] instance for [BitSet]s of a fixed size.
     */
    fun bitSet(sizeBits: Int): BitStore<BitSet> = object : BitStore<BitSet>,
        BitCollection<BitSet> by BitSetBitCollection(sizeBits),
        BitShift<BitSet> by BitSetBitShift(sizeBits),
        Bitwise<BitSet> by BitSetBitwise(sizeBits) {
        override val sizeBits: Int = sizeBits
    }
}
