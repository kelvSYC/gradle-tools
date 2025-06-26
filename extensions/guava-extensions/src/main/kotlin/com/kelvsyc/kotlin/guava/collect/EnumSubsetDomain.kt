package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.DiscreteDomain
import java.math.BigInteger
import java.util.*
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

/**
 * [DiscreteDomain] implementation for sets of enum constants, based on a lexicographical ordering of their
 * [ordinals][Enum.ordinal].
 *
 * @param E The enum type.
 */
class EnumSubsetDomain<E : Enum<E>>(private val enumClass: Class<E>, private val entries: EnumEntries<E>) : DiscreteDomain<EnumSubset<E>>() {
    companion object {
        inline fun <reified E : Enum<E>> of() = EnumSubsetDomain(E::class.java, enumEntries<E>())
    }

    private fun getBitValue(value: EnumSubset<E>): BigInteger {
        return value.fold(BigInteger.ZERO) { acc, e ->
            acc or (BigInteger.ONE shl e.ordinal)
        }
    }

    private fun fromBigInteger(value: BigInteger): EnumSubset<E> {
        val result = EnumSet.noneOf(enumClass)
        for (i in 0 ..< entries.size) {
            if (value.testBit(i)) result.add(entries[i])
        }
        return EnumSubset.of(result)
    }

    override fun next(value: EnumSubset<E>): EnumSubset<E>? {
        return value.takeIf { !it.containsAll(entries) }?.let {
            val newBitValue = getBitValue(it) + BigInteger.ONE
            fromBigInteger(newBitValue)
        }
    }

    override fun previous(value: EnumSubset<E>): EnumSubset<E>? {
        return value.takeIf { it.isNotEmpty() }?.let {
            val newBitValue = getBitValue(it) - BigInteger.ONE
            fromBigInteger(newBitValue)
        }
    }

    override fun distance(start: EnumSubset<E>, end: EnumSubset<E>): Long {
        val newBitValue = getBitValue(end) - getBitValue(start)
        return newBitValue.toLong()
    }

    override fun minValue(): EnumSubset<E> = EnumSubset.of(EnumSet.noneOf(enumClass))
    override fun maxValue(): EnumSubset<E> = EnumSubset.of(EnumSet.allOf(enumClass))
}
