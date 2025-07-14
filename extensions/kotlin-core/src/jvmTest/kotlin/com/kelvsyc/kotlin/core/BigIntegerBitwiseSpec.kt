package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class BigIntegerBitwiseSpec : FunSpec() {
    init {
        test("and") {
            checkAll<Int, Int> { lhs, rhs ->
                val result = BigIntegerBitwise(Int.SIZE_BITS).and(lhs.toBigInteger(), rhs.toBigInteger())
                val processed = result.toInt()

                processed shouldBeEqual (lhs and rhs)
            }
        }

        test("or") {
            checkAll<Int, Int> { lhs, rhs ->
                val result = BigIntegerBitwise(Int.SIZE_BITS).or(lhs.toBigInteger(), rhs.toBigInteger())
                val processed = result.toInt()

                processed shouldBeEqual (lhs or rhs)
            }
        }

        test("xor") {
            checkAll<Int, Int> { lhs, rhs ->
                val result = BigIntegerBitwise(Int.SIZE_BITS).xor(lhs.toBigInteger(), rhs.toBigInteger())
                val processed = result.toInt()

                processed shouldBeEqual (lhs xor rhs)
            }
        }

        test("inv") {
            checkAll<Int> {
                val result = BigIntegerBitwise(Int.SIZE_BITS).inv(it.toBigInteger())
                val processed = result.toInt()

                processed shouldBeEqual it.inv()
            }
        }
    }
}
