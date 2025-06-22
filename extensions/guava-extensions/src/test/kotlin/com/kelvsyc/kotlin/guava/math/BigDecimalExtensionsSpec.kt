package com.kelvsyc.kotlin.guava.math

import com.google.common.math.BigDecimalMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.blocking.forAll
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.BigDecimal
import java.math.RoundingMode

class BigDecimalExtensionsSpec : FunSpec() {
    init {
        test("roundToDouble") {
            mockkStatic(BigDecimalMath::class) {
                forAll<BigDecimal, RoundingMode> { value, mode ->
                    value.roundToDouble(mode)

                    verify {
                        BigDecimalMath.roundToDouble(value, mode)
                    }
                }
            }
        }
    }
}
