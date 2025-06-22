package com.kelvsyc.kotlin.guava.math

import com.google.common.math.BigIntegerMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.BigInteger
import java.math.RoundingMode

class BigIntegerExtensionsSpec : FunSpec() {
    init {
        test("log10") {
            mockkStatic(BigIntegerMath::class) {
                forAll<BigInteger, RoundingMode> { value, mode ->
                    value.log10(mode)

                    verify {
                        BigIntegerMath.log10(value, mode)
                    }
                }
            }
        }

        test("log2") {
            mockkStatic(BigIntegerMath::class) {
                forAll<BigInteger, RoundingMode> { value, mode ->
                    value.log2(mode)

                    verify {
                        BigIntegerMath.log2(value, mode)
                    }
                }
            }
        }

        test("roundToDouble") {
            mockkStatic(BigIntegerMath::class) {
                forAll<BigInteger, RoundingMode> { value, mode ->
                    value.roundToDouble(mode)

                    verify {
                        BigIntegerMath.roundToDouble(value, mode)
                    }
                }
            }
        }

        test("sqrt") {
            mockkStatic(BigIntegerMath::class) {
                forAll<BigInteger, RoundingMode> { value, mode ->
                    value.sqrt(mode)

                    verify {
                        BigIntegerMath.sqrt(value, mode)
                    }
                }
            }
        }

        test("ceilingPowerOfTwo") {
            mockkStatic(BigIntegerMath::class) {
                forAll<BigInteger> {
                    it.ceilingPowerOfTwo

                    verify {
                        BigIntegerMath.ceilingPowerOfTwo(it)
                    }
                }
            }
        }

        test("floorPowerOfTwo") {
            mockkStatic(BigIntegerMath::class) {
                forAll<BigInteger> {
                    it.floorPowerOfTwo

                    verify {
                        BigIntegerMath.floorPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPowerOfTwo") {
            mockkStatic(BigIntegerMath::class) {
                forAll<BigInteger> {
                    it.isPowerOfTwo

                    verify {
                        BigIntegerMath.isPowerOfTwo(it)
                    }
                }
            }
        }
    }
}
