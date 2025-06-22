package com.kelvsyc.kotlin.guava.math

import com.google.common.math.IntMath
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import java.math.RoundingMode

class IntExtensionsSpec : FunSpec() {
    init {
        test("log10") {
            mockkStatic(IntMath::class) {
                checkAll<Int, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { IntMath.log10(any(), any()) } returns 0

                    value.log10(mode)

                    verify {
                        IntMath.log10(value, mode)
                    }
                }
            }
        }

        test("log2") {
            mockkStatic(IntMath::class) {
                checkAll<Int, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { IntMath.log2(any(), any()) } returns 0

                    value.log2(mode)

                    verify {
                        IntMath.log2(value, mode)
                    }
                }
            }
        }

        test("sqrt") {
            mockkStatic(IntMath::class) {
                checkAll<Int, RoundingMode> { value, mode ->
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { IntMath.sqrt(any(), any()) } returns 0

                    value.sqrt(mode)

                    verify {
                        IntMath.sqrt(value, mode)
                    }
                }
            }
        }

        test("ceilingPowerOfTwo") {
            mockkStatic(IntMath::class) {
                checkAll<Int> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { IntMath.ceilingPowerOfTwo(any()) } returns 0

                    it.ceilingPowerOfTwo

                    verify {
                        IntMath.ceilingPowerOfTwo(it)
                    }
                }
            }
        }

        test("floorPowerOfTwo") {
            mockkStatic(IntMath::class) {
                checkAll<Int> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { IntMath.floorPowerOfTwo(any()) } returns 0

                    it.floorPowerOfTwo

                    verify {
                        IntMath.floorPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPowerOfTwo") {
            mockkStatic(IntMath::class) {
                checkAll<Int> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { IntMath.isPowerOfTwo(any()) } returns false

                    it.isPowerOfTwo

                    verify {
                        IntMath.isPowerOfTwo(it)
                    }
                }
            }
        }

        test("isPrime") {
            mockkStatic(IntMath::class) {
                checkAll<Int> {
                    // FIXME workaround for https://github.com/mockk/mockk/issues/929
                    every { IntMath.isPrime(any()) } returns false

                    it.isPrime

                    verify {
                        IntMath.isPrime(it)
                    }
                }
            }
        }
    }
}
