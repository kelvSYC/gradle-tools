package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.BoundType
import com.google.common.collect.Range

/**
 * Returns a Guava [Range] with the same start and end values as this range.
 */
fun <T : Comparable<T>> ClosedRange<T>.toGuavaRange() = Range.closed(start, endInclusive)

/**
 * Returns a Guava [Range] with the same start and end values as this range.
 */
fun <T : Comparable<T>> OpenEndRange<T>.toGuavaRange() = Range.closedOpen(start, endExclusive)

/**
 * Returns the type of this range's lower bound, or `null` if the range is unbounded below.
 */
fun <T : Comparable<T>> Range<T>.lowerBoundTypeOrNull() = takeIf { hasLowerBound() }?.lowerBoundType()

/**
 * Returns the type of this range's upper bound, or `null` if the range is unbounded above.
 */
fun <T : Comparable<T>> Range<T>.upperBoundTypeOrNull() = takeIf { hasUpperBound() }?.upperBoundType()

/**
 * Returns the lower endpoint of this range, or `null` if the range is unbounded below.
 */
fun <T : Comparable<T>> Range<T>.lowerEndpointOrNull() = takeIf { hasLowerBound() }?.lowerEndpoint()

/**
 * Returns the upper endpoint of this range, or `null` if the range is unbounded above.
 */
fun <T : Comparable<T>> Range<T>.upperEndpointOrNull() = takeIf { hasUpperBound() }?.upperEndpoint()

/**
 * Returns this range as a Kotlin [ClosedRange], or `null` if this range does not represent a closed range.
 */
fun <T : Comparable<T>> Range<T>.toClosedRange() = takeIf {
    lowerBoundTypeOrNull() == BoundType.CLOSED && upperBoundTypeOrNull() == BoundType.CLOSED
}?.let {
    lowerEndpoint()..upperEndpoint()
}

/**
 * Returns this range as a Kotlin [OpenEndRange], or `null` if this range does not represent an open end range.
 */
fun <T : Comparable<T>> Range<T>.toOpenEndRange() = takeIf {
    lowerBoundTypeOrNull() == BoundType.CLOSED && upperBoundTypeOrNull() == BoundType.OPEN
}?.let {
    lowerEndpoint() ..< upperEndpoint()
}
