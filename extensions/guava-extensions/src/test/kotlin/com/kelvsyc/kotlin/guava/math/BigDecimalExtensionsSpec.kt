package com.kelvsyc.kotlin.guava.math

import com.google.common.math.BigDecimalMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.BigDecimal
import java.math.RoundingMode

class BigDecimalExtensionsSpec : FunSpec() {
    init {
        test("roundToDouble") {
            mockkStatic(BigDecimalMath::class) {
                checkAll<BigDecimal, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { BigDecimalMath.roundToDouble(any(), any()) } returns 0.0

                    value.roundToDouble(mode)

                    verify {
                        BigDecimalMath.roundToDouble(value, mode)
                    }
                }
            }
        }
    }
}
