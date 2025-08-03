package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.math.BigInteger
import java.util.*

class BitStoreConstantsJVMSpec : FunSpec() {
    init {
        context("BigInteger") {
            val sized = object : Sized<BigInteger> {
                override val sizeBits: Int = Int.SIZE_BITS
            }
            val traits = BigIntegerBitStoreConstants(sized)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.bigInt(Int.SIZE_BITS).filter { it != BigInteger.ZERO }) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("BitSet") {
            val sized = object : Sized<BitSet> {
                override val sizeBits: Int = Int.SIZE_BITS
            }
            val traits = BitSetBitStoreConstants(sized)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.int().filter { it != 0 }) {
                    val bytes = TypeTraits.Int.asByteArray(it)
                    val value = BitSet.valueOf(bytes)
                    
                    traits.hasSetBits(value).shouldBeTrue()
                    traits.isAllClear(value).shouldBeFalse()
                }
            }
        }
    }
}
