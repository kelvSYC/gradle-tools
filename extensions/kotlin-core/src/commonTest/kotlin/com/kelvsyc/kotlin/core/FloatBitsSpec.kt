package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class FloatBitsSpec : FunSpec() {
    companion object {
        val SIGNIFICAND_MASK = (1 shl TypeTraits.Float.mantissaWidth) - 1
        val EXPONENT_MASK = (1 shl TypeTraits.Float.exponentWidth) - 1
        val SIGN_MASK = 1U shl (TypeTraits.Float.sizeBits - 1)
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
                bits.biasedExponent shouldBeEqual ((it.toBits() ushr (TypeTraits.Float.precision - 1)) and EXPONENT_MASK)
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
