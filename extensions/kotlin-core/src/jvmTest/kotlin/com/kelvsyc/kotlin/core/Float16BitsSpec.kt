package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class Float16BitsSpec : FunSpec() {
    companion object {
        const val SIGNIFICAND_MASK = (1 shl (AbstractBinary16Bits.PRECISION - 1)) - 1
        const val EXPONENT_MASK = (1 shl AbstractBinary16Bits.EXPONENT_WIDTH) - 1
        val SIGN_MASK = 1 shl (AbstractBinary16Bits.SIZE_BITS - 1)
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
                bits.biasedExponent shouldBeEqual ((it.toInt() ushr (AbstractBinary16Bits.PRECISION - 1)) and EXPONENT_MASK)
                bits.signBit shouldBeEqual ((it.toInt() and SIGN_MASK) != 0)
            }
        }
    }
}
