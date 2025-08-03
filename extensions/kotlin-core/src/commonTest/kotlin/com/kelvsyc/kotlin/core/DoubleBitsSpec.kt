package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class DoubleBitsSpec : FunSpec() {
    companion object {
        val SIGNIFICAND_MASK = (1L shl (TypeTraits.Double.precision - 1)) - 1
        val EXPONENT_MASK = (1L shl TypeTraits.Double.exponentWidth) - 1
        val SIGN_MASK = 1UL shl (TypeTraits.Double.sizeBits - 1)
    }

    init {
        test("create from bits") {
            checkAll<Double> {
                val bits = DoubleBits(it.toBits())

                bits.toFloatingPoint() shouldBeEqual it
            }
        }

        test("create from value") {
            checkAll<Double> {
                val bits = DoubleBits(it)

                bits.toFloatingPoint() shouldBeEqual it
            }
        }

        test("fields") {
            checkAll<Double> {
                val bits = DoubleBits(it)

                bits.mantissa shouldBeEqual (it.toBits() and SIGNIFICAND_MASK)
                bits.biasedExponent shouldBeEqual ((it.toBits() ushr TypeTraits.Double.mantissaWidth) and EXPONENT_MASK).toInt()
                bits.signBit shouldBeEqual ((it.toBits().toULong() and SIGN_MASK) != 0UL)
            }
        }

        test("properties") {
            checkAll<Double> {
                val bits = DoubleBits(it)

                bits.isFinite shouldBeEqual it.isFinite()
                bits.isInfinite shouldBeEqual it.isInfinite()
                bits.isNaN shouldBeEqual it.isNaN()
            }
        }
    }
}
