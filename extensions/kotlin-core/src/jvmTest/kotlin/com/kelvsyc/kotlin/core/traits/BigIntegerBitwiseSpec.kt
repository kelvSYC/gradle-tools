package com.kelvsyc.kotlin.core.traits

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import java.math.BigInteger

class BigIntegerBitwiseSpec : FunSpec() {
    init {
        val sized = object : Sized<BigInteger> {
            override val sizeBits: Int = Int.SIZE_BITS
        }
        val traits = BigIntegerBitwise(sized)

        test("and") {
            checkAll<Int, Int> { lhs, rhs ->
                val result = traits.and(lhs.toBigInteger(), rhs.toBigInteger())
                val processed = result.toInt()

                processed shouldBeEqual (lhs and rhs)
            }
        }

        test("or") {
            checkAll<Int, Int> { lhs, rhs ->
                val result = traits.or(lhs.toBigInteger(), rhs.toBigInteger())
                val processed = result.toInt()

                processed shouldBeEqual (lhs or rhs)
            }
        }

        test("xor") {
            checkAll<Int, Int> { lhs, rhs ->
                val result = traits.xor(lhs.toBigInteger(), rhs.toBigInteger())
                val processed = result.toInt()

                processed shouldBeEqual (lhs xor rhs)
            }
        }

        test("inv") {
            checkAll<Int> {
                val result = traits.inv(it.toBigInteger())
                val processed = result.toInt()

                processed shouldBeEqual it.inv()
            }
        }
    }
}
