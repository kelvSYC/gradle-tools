package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class Float16Spec : FunSpec() {
    init {
        test("FloatingPoint") {
            val traits = Float16.Traits
            checkAll<Short> {
                val value = Float16(it)
                traits.isFinite(value) shouldBeEqual Float16Bits.ofValue(value).isFinite
            }
            checkAll<Short> {
                val value = Float16(it)
                traits.isInfinite(value) shouldBeEqual Float16Bits.ofValue(value).isInfinite
            }
            checkAll<Short> {
                val value = Float16(it)
                traits.isNaN(value) shouldBeEqual Float16Bits.ofValue(value).isNaN
            }
        }

        test("Addition Float") {
            val traits = Float16.Traits
            checkAll<Short, Short> { lhsRaw, rhsRaw ->
                val lhs = Float16(lhsRaw)
                val rhs = Float16(rhsRaw)
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs)
            }
            checkAll<Short, Short> { lhsRaw, rhsRaw ->
                val lhs = Float16(lhsRaw)
                val rhs = Float16(rhsRaw)
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs)
            }
        }

        test("Multiplication Float") {
            val traits = Float16.Traits
            checkAll<Short, Short> { lhsRaw, rhsRaw ->
                val lhs = Float16(lhsRaw)
                val rhs = Float16(rhsRaw)
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs)
            }
            checkAll<Short, Short> { lhsRaw, rhsRaw ->
                val lhs = Float16(lhsRaw)
                val rhs = Float16(rhsRaw)
                traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs)
            }
        }

        test("Comparator") {
            checkAll<Short, Short> { lhsRaw, rhsRaw ->
                val lhs = Float16(lhsRaw)
                val rhs = Float16(rhsRaw)

                lhs.compareTo(rhs) shouldBeEqual lhs.toFloat().compareTo(rhs.toFloat())
            }
        }
    }
}
