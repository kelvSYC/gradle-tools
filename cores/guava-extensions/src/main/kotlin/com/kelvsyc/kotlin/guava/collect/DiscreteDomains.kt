package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.DiscreteDomain

object DiscreteDomains {
    /**
     * Implementation of a [DiscreteDomain] over [Short] instances.
     */
    object ShortDomain : DiscreteDomain<Short>() {
        override fun distance(start: Short, end: Short): Long = (end - start).toLong()

        override fun next(value: Short): Short? = value.takeIf { it < Short.MAX_VALUE }?.let { (it + 1).toShort() }

        override fun previous(value: Short): Short? = value.takeIf { it > Short.MIN_VALUE }?.let { (it - 1).toShort() }
    }

    /**
     * Implementation of a [DiscreteDomain] over [UShort] instances.
     */
    object UShortDomain: DiscreteDomain<UShort>() {
        override fun distance(start: UShort, end: UShort): Long = (end - start).toLong()

        override fun next(value: UShort): UShort? = value.takeIf { it < UShort.MAX_VALUE }?.let { (it + 1u).toUShort() }

        override fun previous(value: UShort): UShort? = value.takeIf { it > UShort.MIN_VALUE }?.let { (it - 1u).toUShort() }
    }

    /**
     * Implementation of a [DiscreteDomain] over [UInt] instances.
     */
    object UIntDomain : DiscreteDomain<UInt>() {
        override fun distance(start: UInt, end: UInt): Long = (end - start).toLong()

        override fun next(value: UInt): UInt? = value.takeIf { it < UInt.MAX_VALUE }?.let { it + 1.toUInt() }

        override fun previous(value: UInt): UInt? = value.takeIf { it > UInt.MIN_VALUE }?.let { it - 1.toUInt() }
    }

    /**
     * Implementation of a [DiscreteDomain] over [ULong] instances.
     */
    object ULongDomain: DiscreteDomain<ULong>() {
        override fun distance(start: ULong, end: ULong): Long = (end - start).toLong()

        override fun next(value: ULong): ULong? = value.takeIf { it < ULong.MAX_VALUE }?.let { it + 1.toULong() }

        override fun previous(value: ULong): ULong? = value.takeIf { it > ULong.MIN_VALUE }?.let { it - 1.toULong() }
    }
}
