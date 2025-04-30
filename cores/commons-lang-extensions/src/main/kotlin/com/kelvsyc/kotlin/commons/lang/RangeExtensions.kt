package com.kelvsyc.kotlin.commons.lang

import org.apache.commons.lang3.Range

/**
 * Returns a Commons [Range] with the same start and end values as this range.
 */
fun <T : Comparable<T>> ClosedRange<T>.toCommonsRange() = Range.of(start, endInclusive)

/**
 * Returns this range as a Kotlin [ClosedRange].
 */
fun <T : Comparable<T>> Range<T>.toClosedRange() = minimum.rangeTo(maximum)
