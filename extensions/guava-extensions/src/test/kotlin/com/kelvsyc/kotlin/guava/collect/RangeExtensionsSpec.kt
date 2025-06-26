package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.BoundType
import com.google.common.collect.Range
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.intRange
import io.kotest.property.checkAll

class RangeExtensionsSpec : FunSpec() {
    data class RangeData(
        val range: Range<Int>,
        val expectedLowerBoundType: BoundType?,
        val expectedUpperBoundType: BoundType?,
        val expectedLowerBound: Int?,
        val expectedUpperBound: Int?,
        val expectedClosedRange: ClosedRange<Int>?,
        val expectedOpenEndRange: OpenEndRange<Int>?
    )
    val rangeData = mapOf(
        "open" to RangeData(Range.open(1, 2), BoundType.OPEN, BoundType.OPEN, 1, 2, null, null),
        "closed" to RangeData(Range.closed(1, 2), BoundType.CLOSED, BoundType.CLOSED, 1, 2, 1 .. 2, null),
        "closedOpen" to RangeData(Range.closedOpen(1, 2), BoundType.CLOSED, BoundType.OPEN, 1, 2, null, 1 ..< 2),
        "openClosed" to RangeData(Range.openClosed(1, 2), BoundType.OPEN, BoundType.CLOSED, 1, 2, null, null),
        "greaterThan" to RangeData(Range.greaterThan(1), BoundType.OPEN, null, 1, null, null, null),
        "atLeast" to RangeData(Range.atLeast(1), BoundType.CLOSED, null, 1, null, null, null),
        "lessThan" to RangeData(Range.lessThan(2), null, BoundType.OPEN, null, 2, null, null),
        "atMost" to RangeData(Range.atMost(2), null, BoundType.CLOSED, null, 2, null, null),
        "all" to RangeData(Range.all(), null, null, null, null, null, null)
    )

    init {
        test("closed to Guava") {
            checkAll<ClosedRange<Int>>(Arb.intRange(0 .. 100).filter {
                // Closed ranges are not empty
                !it.isEmpty()
            }) {
                val guava = it.toGuavaRange()

                guava.lowerBoundType() shouldBeEqual BoundType.CLOSED
                guava.upperBoundType() shouldBeEqual BoundType.CLOSED
                guava.lowerEndpoint() shouldBeEqual it.start
                guava.upperEndpoint() shouldBeEqual it.endInclusive
            }
        }

        test("open to Guava") {
            checkAll<OpenEndRange<Int>>(Arb.intRange(0 .. 100)) {
                val guava = it.toGuavaRange()

                guava.lowerBoundType() shouldBeEqual BoundType.CLOSED
                guava.upperBoundType() shouldBeEqual BoundType.OPEN
                guava.lowerEndpoint() shouldBeEqual it.start
                guava.upperEndpoint() shouldBeEqual it.endExclusive
            }
        }

        context("lowerBoundTypeOrNull") {
            withData(rangeData) {
                it.range.lowerBoundTypeOrNull() shouldBe it.expectedLowerBoundType
            }
        }

        context("upperBoundTypeOrNull") {
            withData(rangeData) {
                it.range.upperBoundTypeOrNull() shouldBe it.expectedUpperBoundType
            }
        }

        context("lowerEndpointOrNull") {
            withData(rangeData) {
                it.range.lowerEndpointOrNull() shouldBe it.expectedLowerBound
            }
        }

        context("upperEndpointOrNull") {
            withData(rangeData) {
                it.range.upperEndpointOrNull() shouldBe it.expectedUpperBound
            }
        }

        context("toClosedRange") {
            withData(rangeData) {
                if (it.expectedClosedRange == null) {
                    it.range.toClosedRange() shouldBe null
                } else {
                    it.range.toClosedRange()?.let { range ->
                        range.start shouldBeEqual it.expectedClosedRange.start
                        range.endInclusive shouldBeEqual it.expectedClosedRange.endInclusive
                    }
                }
            }
        }

        context("toOpenEndRange") {
            withData(rangeData) {
                if (it.expectedOpenEndRange == null) {
                    it.range.toOpenEndRange() shouldBe null
                } else {
                    it.range.toOpenEndRange()?.let { range ->
                        range.start shouldBeEqual it.expectedOpenEndRange.start
                        range.endExclusive shouldBeEqual it.expectedOpenEndRange.endExclusive
                    }
                }
            }
        }
    }
}
