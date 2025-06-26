package com.kelvsyc.kotlin.guava.math

import com.google.common.math.LongMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.RoundingMode

class LongExtensionsSpec : FunSpec() {
    init {
        test("log10") {
            mockkStatic(LongMath::class) {
                checkAll<Long, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { LongMath.log10(any(), any()) } returns 0

                    value.log10(mode)

                    verify {
                        LongMath.log10(value, mode)
                    }
                }
            }
        }

        test("log2") {
            mockkStatic(LongMath::class) {
                checkAll<Long, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { LongMath.log2(any(), any()) } returns 0

                    value.log2(mode)

                    verify {
                        LongMath.log2(value, mode)
                    }
                }
            }
        }

        test("roundToDouble") {
            mockkStatic(LongMath::class) {
                checkAll<Long, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { LongMath.roundToDouble(any(), any()) } returns 0.0

                    value.roundToDouble(mode)

                    verify {
                        LongMath.roundToDouble(value, mode)
                    }
                }
            }
        }


        test("sqrt") {
            mockkStatic(LongMath::class) {
                checkAll<Long, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { LongMath.sqrt(any(), any()) } returns 0L

                    value.sqrt(mode)

                    verify {
                        LongMath.sqrt(value, mode)
                    }
                }
            }
        }
        test("ceilingPowerOfTwo") {
            mockkStatic(LongMath::class) {
                checkAll<Long> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { LongMath.ceilingPowerOfTwo(any()) } returns 0L

                    it.ceilingPowerOfTwo

                    verify {
                        LongMath.ceilingPowerOfTwo(it)
                    }
                }
            }
        }

        test("floorPowerOfTwo") {
            mockkStatic(LongMath::class) {
                checkAll<Long> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { LongMath.floorPowerOfTwo(any()) } returns 0L

                    it.floorPowerOfTwo

                    verify {
                        LongMath.floorPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPowerOfTwo") {
            mockkStatic(LongMath::class) {
                checkAll<Long> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { LongMath.isPowerOfTwo(any()) } returns false

                    it.isPowerOfTwo

                    verify {
                        LongMath.isPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPrime") {
            mockkStatic(LongMath::class) {
                checkAll<Long> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { LongMath.isPrime(any()) } returns false

                    it.isPrime

                    verify {
                        LongMath.isPrime(it)
                    }
                }
            }
        }
    }
}
