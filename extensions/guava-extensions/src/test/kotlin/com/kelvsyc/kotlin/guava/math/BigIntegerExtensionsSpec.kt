package com.kelvsyc.kotlin.guava.math

import com.google.common.math.BigIntegerMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.BigInteger
import java.math.RoundingMode

class BigIntegerExtensionsSpec : FunSpec() {
    init {
        test("log10") {
            mockkStatic(BigIntegerMath::class) {
                checkAll<BigInteger, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { BigIntegerMath.log10(any(), any()) } returns 0

                    value.log10(mode)

                    verify {
                        BigIntegerMath.log10(value, mode)
                    }
                }
            }
        }

        test("log2") {
            mockkStatic(BigIntegerMath::class) {
                checkAll<BigInteger, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { BigIntegerMath.log2(any(), any()) } returns 0

                    value.log2(mode)

                    verify {
                        BigIntegerMath.log2(value, mode)
                    }
                }
            }
        }

        test("roundToDouble") {
            mockkStatic(BigIntegerMath::class) {
                checkAll<BigInteger, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { BigIntegerMath.roundToDouble(any(), any()) } returns 0.0

                    value.roundToDouble(mode)

                    verify {
                        BigIntegerMath.roundToDouble(value, mode)
                    }
                }
            }
        }

        test("sqrt") {
            mockkStatic(BigIntegerMath::class) {
                checkAll<BigInteger, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { BigIntegerMath.sqrt(any(), any()) } returns mockk()

                    value.sqrt(mode)

                    verify {
                        BigIntegerMath.sqrt(value, mode)
                    }
                }
            }
        }

        test("ceilingPowerOfTwo") {
            mockkStatic(BigIntegerMath::class) {
                checkAll<BigInteger> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { BigIntegerMath.ceilingPowerOfTwo(any()) } returns mockk()

                    it.ceilingPowerOfTwo

                    verify {
                        BigIntegerMath.ceilingPowerOfTwo(it)
                    }
                }
            }
        }

        test("floorPowerOfTwo") {
            mockkStatic(BigIntegerMath::class) {
                checkAll<BigInteger> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { BigIntegerMath.floorPowerOfTwo(any()) } returns mockk()

                    it.floorPowerOfTwo

                    verify {
                        BigIntegerMath.floorPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPowerOfTwo") {
            mockkStatic(BigIntegerMath::class) {
                checkAll<BigInteger> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { BigIntegerMath.isPowerOfTwo(any()) } returns false

                    it.isPowerOfTwo

                    verify {
                        BigIntegerMath.isPowerOfTwo(it)
                    }
                }
            }
        }
    }
}
