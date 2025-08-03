package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

class Int128Spec : FunSpec() {
    val arb = Arb.bind(Arb.long(), Arb.long(), ::Int128)

    init {
        test("conversion roundtrip") {
            checkAll<Long, Long> { high, low ->
                val value = Int128(high, low)

                val roundtrip = Int128.converter.reverse(Int128.converter(value))

                roundtrip.high shouldBeEqual high
                roundtrip.low shouldBeEqual low
            }
        }

        test("comparator") {
            checkAll(arb, arb) { lhs, rhs ->
                lhs.compareTo(rhs) shouldBeEqual lhs.toBigInteger().compareTo(rhs.toBigInteger())
            }
        }
    }
}
