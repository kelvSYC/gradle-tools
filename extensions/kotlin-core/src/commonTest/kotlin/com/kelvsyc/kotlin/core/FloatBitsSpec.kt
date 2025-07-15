package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class FloatBitsSpec : FunSpec() {
    companion object {
        const val SIGNIFICAND_MASK = (1 shl (FloatBits.PRECISION - 1)) - 1
        const val EXPONENT_MASK = (1 shl FloatBits.EXPONENT_WIDTH) - 1
        val SIGN_MASK = 1U shl (FloatBits.SIZE_BITS - 1)
    }

    init {
        test("create from bits") {
            checkAll<Float> {
                val bits = FloatBits(it.toBits())

                bits.toFloatingPoint() shouldBeEqual it
            }
        }

        test("create from value") {
            checkAll<Float> {
                val bits = FloatBits(it)

                bits.toFloatingPoint() shouldBeEqual it
            }
        }

        test("fields") {
            checkAll<Float> {
                val bits = FloatBits(it)

                bits.mantissa shouldBeEqual (it.toBits() and SIGNIFICAND_MASK)
                bits.biasedExponent shouldBeEqual ((it.toBits() ushr (FloatBits.PRECISION - 1)) and EXPONENT_MASK)
                bits.signBit shouldBeEqual ((it.toBits().toUInt() and SIGN_MASK) != 0U)
            }
        }

        test("properties") {
            checkAll<Float> {
                val bits = FloatBits(it)

                bits.isFinite shouldBeEqual it.isFinite()
                bits.isInfinite shouldBeEqual it.isInfinite()
                bits.isNaN shouldBeEqual it.isNaN()
            }
        }
    }
}
