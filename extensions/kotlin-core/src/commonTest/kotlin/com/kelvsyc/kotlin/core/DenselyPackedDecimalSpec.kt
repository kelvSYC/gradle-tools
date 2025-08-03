package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class DenselyPackedDecimalSpec : FunSpec() {
    init {
        test("roundtrip") {
            checkAll(Arb.int(0, 999)) {
                val encoded = DenselyPackedDecimal.of(it)

                encoded.asNumber shouldBeEqual it
            }
        }

        test("asString") {
            checkAll(Arb.int(0, 999)) {
                val encoded = DenselyPackedDecimal.of(it)

                encoded.asString shouldBeEqual it.toString().padStart(3, '0')
            }
        }
    }
}
