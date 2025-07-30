package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.BigIntegerBitShift
import com.kelvsyc.kotlin.core.traits.BigIntegerBitwise
import com.kelvsyc.kotlin.core.traits.BitSetBitShift
import com.kelvsyc.kotlin.core.traits.BitSetBitwise
import com.kelvsyc.kotlin.core.traits.BitShift
import com.kelvsyc.kotlin.core.traits.BitStore
import com.kelvsyc.kotlin.core.traits.Sized
import java.math.BigInteger
import java.util.*

/**
 * Functions relating to obtaining [BitStore] instances from JVM types
 */
object JvmBitStores {
    /**
     * Creates a [BitStore] instance for [BigInteger]s of a fixed size.
     */
    fun bigInteger(sized: Sized<BigInteger>): BitStore<BigInteger> = object : BitStore<BigInteger>,
        Sized<BigInteger> by sized,
        BitCollection<BigInteger> by BigIntegerBitCollection(sized),
        BitShift<BigInteger> by BigIntegerBitShift(sized),
        Bitwise<BigInteger> by BigIntegerBitwise(sized) {}

    /**
     * Creates a [BitStore] instance for [BitSet]s of a fixed size.
     */
    fun bitSet(sized: Sized<BitSet>): BitStore<BitSet> = object : BitStore<BitSet>,
        Sized<BitSet> by sized,
        BitCollection<BitSet> by BitSetBitCollection(sized),
        BitShift<BitSet> by BitSetBitShift(sized),
        Bitwise<BitSet> by BitSetBitwise(sized) {}
}
