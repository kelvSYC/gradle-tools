package com.kelvsyc.kotlin.guava.math

import com.google.common.math.LongMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.RoundingMode

class LongExtensionsSpec : FunSpec() {
    init {
        test("log10") {
            mockkStatic(LongMath::class) {
                forAll<Long, RoundingMode> { value, mode ->
                    value.log10(mode)

                    verify {
                        LongMath.log10(value, mode)
                    }
                }
            }
        }

        test("log2") {
            mockkStatic(LongMath::class) {
                forAll<Long, RoundingMode> { value, mode ->
                    value.log2(mode)

                    verify {
                        LongMath.log2(value, mode)
                    }
                }
            }
        }

        test("roundToDouble") {
            mockkStatic(LongMath::class) {
                forAll<Long, RoundingMode> { value, mode ->
                    value.roundToDouble(mode)

                    verify {
                        LongMath.roundToDouble(value, mode)
                    }
                }
            }
        }


        test("sqrt") {
            mockkStatic(LongMath::class) {
                forAll<Long, RoundingMode> { value, mode ->
                    value.sqrt(mode)

                    verify {
                        LongMath.sqrt(value, mode)
                    }
                }
            }
        }
        test("ceilingPowerOfTwo") {
            mockkStatic(LongMath::class) {
                forAll<Long> {
                    it.ceilingPowerOfTwo

                    verify {
                        LongMath.ceilingPowerOfTwo(it)
                    }
                }
            }
        }

        test("floorPowerOfTwo") {
            mockkStatic(LongMath::class) {
                forAll<Long> {
                    it.floorPowerOfTwo

                    verify {
                        LongMath.floorPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPowerOfTwo") {
            mockkStatic(LongMath::class) {
                forAll<Long> {
                    it.isPowerOfTwo

                    verify {
                        LongMath.isPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPrime") {
            mockkStatic(LongMath::class) {
                forAll<Long> {
                    it.isPrime

                    verify {
                        LongMath.isPrime(it)
                    }
                }
            }
        }
    }
}
