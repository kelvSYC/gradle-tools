package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class Float16BitsSpec : FunSpec() {
    companion object {
        val SIGNIFICAND_MASK = (1 shl (Float16.Traits.precision - 1)) - 1
        val EXPONENT_MASK = (1 shl Float16.Traits.exponentWidth) - 1
        val SIGN_MASK = 1 shl (Float16.Traits.sizeBits - 1)
    }

    init {
        test("create from bits") {
            checkAll<Short> {
                val value = Float16(it)
                val bits = Float16Bits.ofBits(it)

                bits.toFloatingPoint() shouldBeEqual value
            }
        }

        test("create from value") {
            checkAll<Short> {
                val value = Float16(it)
                val bits = Float16Bits.ofValue(value)

                bits.toFloatingPoint() shouldBeEqual value
            }
        }

        test("fields") {
            checkAll<Short> {
                val bits = Float16Bits.ofBits(it)

                bits.mantissa shouldBeEqual (it.toInt() and SIGNIFICAND_MASK).toShort()
                bits.biasedExponent shouldBeEqual ((it.toInt() ushr (Float16.Traits.mantissaWidth)) and EXPONENT_MASK)
                bits.signBit shouldBeEqual ((it.toInt() and SIGN_MASK) != 0)
            }
        }
    }
}
