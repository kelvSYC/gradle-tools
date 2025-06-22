package com.kelvsyc.kotlin.guava.math

import com.google.common.math.DoubleMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.RoundingMode

class DoubleExtensionsSpec : FunSpec() {
    init {
        test("log2 (rounding)") {
            mockkStatic(DoubleMath::class) {
                checkAll<Double, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { DoubleMath.log2(any(), any()) } returns 0

                    value.log2(mode)

                    verify {
                        DoubleMath.log2(value, mode)
                    }
                }
            }
        }

        test("roundToInt") {
            mockkStatic(DoubleMath::class) {
                checkAll<Double, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { DoubleMath.roundToInt(any(), any()) } returns 0

                    value.roundToInt(mode)

                    verify {
                        DoubleMath.roundToInt(value, mode)
                    }
                }
            }
        }

        test("roundToLong") {
            mockkStatic(DoubleMath::class) {
                checkAll<Double, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { DoubleMath.roundToLong(any(), any()) } returns 0L

                    value.roundToLong(mode)

                    verify {
                        DoubleMath.roundToLong(value, mode)
                    }
                }
            }
        }

        test("roundToBigInteger") {
            mockkStatic(DoubleMath::class) {
                checkAll<Double, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { DoubleMath.roundToBigInteger(any(), any()) } returns mockk()

                    value.roundToBigInteger(mode)

                    verify {
                        DoubleMath.roundToBigInteger(value, mode)
                    }
                }
            }
        }

        test("isMathematicalInteger") {
            mockkStatic(DoubleMath::class) {
                checkAll<Double> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { DoubleMath.isMathematicalInteger(any()) } returns false

                    it.isMathematicalInteger

                    verify {
                        DoubleMath.isMathematicalInteger(it)
                    }
                }
            }
        }

        test("isPowerOfTwo") {
            mockkStatic(DoubleMath::class) {
                checkAll<Double> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { DoubleMath.isPowerOfTwo(any()) } returns false

                    it.isPowerOfTwo

                    verify {
                        DoubleMath.isPowerOfTwo(it)
                    }
                }
            }
        }

        test("log2") {
            mockkStatic(DoubleMath::class) {
                checkAll<Double> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { DoubleMath.log2(any()) } returns 0.0

                    it.log2

                    verify {
                        DoubleMath.log2(it)
                    }
                }
            }
        }
    }
}
