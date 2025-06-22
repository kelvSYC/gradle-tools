package com.kelvsyc.kotlin.guava.math

import com.google.common.math.DoubleMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.RoundingMode

class DoubleExtensionsSpec : FunSpec() {
    init {
        test("log2 (rounding)") {
            mockkStatic(DoubleMath::class) {
                forAll<Double, RoundingMode> { value, mode ->
                    value.log2(mode)

                    verify {
                        DoubleMath.log2(value, mode)
                    }
                }
            }
        }

        test("roundToInt") {
            mockkStatic(DoubleMath::class) {
                forAll<Double, RoundingMode> { value, mode ->
                    value.roundToInt(mode)

                    verify {
                        DoubleMath.roundToInt(value, mode)
                    }
                }
            }
        }

        test("roundToLong") {
            mockkStatic(DoubleMath::class) {
                forAll<Double, RoundingMode> { value, mode ->
                    value.roundToLong(mode)

                    verify {
                        DoubleMath.roundToLong(value, mode)
                    }
                }
            }
        }

        test("roundToBigInteger") {
            mockkStatic(DoubleMath::class) {
                forAll<Double, RoundingMode> { value, mode ->
                    value.roundToBigInteger(mode)

                    verify {
                        DoubleMath.roundToBigInteger(value, mode)
                    }
                }
            }
        }

        test("isMathematicalInteger") {
            mockkStatic(DoubleMath::class) {
                forAll<Double> {
                    it.isMathematicalInteger

                    verify {
                        DoubleMath.isMathematicalInteger(it)
                    }
                }
            }
        }

        test("isPowerOfTwo") {
            mockkStatic(DoubleMath::class) {
                forAll<Double> {
                    it.isPowerOfTwo

                    verify {
                        DoubleMath.isPowerOfTwo(it)
                    }
                }
            }
        }

        test("log2") {
            mockkStatic(DoubleMath::class) {
                forAll<Double> {
                    it.log2

                    verify {
                        DoubleMath.log2(it)
                    }
                }
            }
        }
    }
}
