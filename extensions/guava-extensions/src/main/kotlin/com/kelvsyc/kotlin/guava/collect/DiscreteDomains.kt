package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.DiscreteDomain

object DiscreteDomains {
    /**
     * Implementation of a [DiscreteDomain] over [Short] instances.
     */
    object ShortDomain : DiscreteDomain<Short>() {
        override fun distance(start: Short, end: Short): Long = end.toLong() - start.toLong()

        override fun next(value: Short): Short? = value.takeIf { it < Short.MAX_VALUE }?.inc()

        override fun previous(value: Short): Short? = value.takeIf { it > Short.MIN_VALUE }?.dec()

        override fun maxValue(): Short = Short.MAX_VALUE

        override fun minValue(): Short = Short.MIN_VALUE
    }

    /**
     * Implementation of a [DiscreteDomain] over [UShort] instances.
     */
    object UShortDomain: DiscreteDomain<UShort>() {
        override fun distance(start: UShort, end: UShort): Long = end.toLong() - start.toLong()

        override fun next(value: UShort): UShort? = value.takeIf { it < UShort.MAX_VALUE }?.inc()

        override fun previous(value: UShort): UShort? = value.takeIf { it > UShort.MIN_VALUE }?.dec()

        override fun maxValue(): UShort = UShort.MAX_VALUE

        override fun minValue(): UShort = UShort.MIN_VALUE
    }

    /**
     * Implementation of a [DiscreteDomain] over [UInt] instances.
     */
    object UIntDomain : DiscreteDomain<UInt>() {
        override fun distance(start: UInt, end: UInt): Long = end.toLong() - start.toLong()

        override fun next(value: UInt): UInt? = value.takeIf { it < UInt.MAX_VALUE }?.inc()

        override fun previous(value: UInt): UInt? = value.takeIf { it > UInt.MIN_VALUE }?.dec()

        override fun maxValue(): UInt = UInt.MAX_VALUE

        override fun minValue(): UInt = UInt.MIN_VALUE
    }

    /**
     * Implementation of a [DiscreteDomain] over [ULong] instances.
     */
    object ULongDomain: DiscreteDomain<ULong>() {
        // The distance between two ULongs might not fit in a Long, so we need to coerce the result into a Long, as
        // required by the contract of distance()
        override fun distance(start: ULong, end: ULong): Long = if (start < end) {
            val difference = end - start
            difference.coerceAtMost(Long.MAX_VALUE.toULong()).toLong()
        } else if (start > end) {
            val difference = start - end
            if (difference >= Long.MAX_VALUE.toULong() + 1UL) {
                // The difference is too big to fit as a negative Long value (or is exactly Long.MIN_VALUE)
                Long.MIN_VALUE
            } else {
                -difference.coerceAtMost(Long.MAX_VALUE.toULong()).toLong()
            }
        } else 0L

        override fun next(value: ULong): ULong? = value.takeIf { it < ULong.MAX_VALUE }?.inc()

        override fun previous(value: ULong): ULong? = value.takeIf { it > ULong.MIN_VALUE }?.dec()

        override fun maxValue(): ULong = ULong.MAX_VALUE

        override fun minValue(): ULong = ULong.MIN_VALUE
    }
}
