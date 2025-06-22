package com.kelvsyc.kotlin.guava.math

import com.google.common.math.IntMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.RoundingMode

class IntExtensionsSpec : FunSpec() {
    init {
        test("log10") {
            mockkStatic(IntMath::class) {
                forAll<Int, RoundingMode> { value, mode ->
                    value.log10(mode)

                    verify {
                        IntMath.log10(value, mode)
                    }
                }
            }
        }

        test("log2") {
            mockkStatic(IntMath::class) {
                forAll<Int, RoundingMode> { value, mode ->
                    value.log2(mode)

                    verify {
                        IntMath.log2(value, mode)
                    }
                }
            }
        }

        test("sqrt") {
            mockkStatic(IntMath::class) {
                forAll<Int, RoundingMode> { value, mode ->
                    value.sqrt(mode)

                    verify {
                        IntMath.sqrt(value, mode)
                    }
                }
            }
        }

        test("ceilingPowerOfTwo") {
            mockkStatic(IntMath::class) {
                forAll<Int> {
                    it.ceilingPowerOfTwo

                    verify {
                        IntMath.ceilingPowerOfTwo(it)
                    }
                }
            }
        }

        test("floorPowerOfTwo") {
            mockkStatic(IntMath::class) {
                forAll<Int> {
                    it.floorPowerOfTwo

                    verify {
                        IntMath.floorPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPowerOfTwo") {
            mockkStatic(IntMath::class) {
                forAll<Int> {
                    it.isPowerOfTwo

                    verify {
                        IntMath.isPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPrime") {
            mockkStatic(IntMath::class) {
                forAll<Int> {
                    it.isPrime

                    verify {
                        IntMath.isPrime(it)
                    }
                }
            }
        }
    }
}
