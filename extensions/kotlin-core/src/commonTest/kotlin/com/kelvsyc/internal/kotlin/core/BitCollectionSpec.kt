package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

@OptIn(ExperimentalStdlibApi::class)
class BitCollectionSpec : FunSpec() {
    init {
        test("Byte") {
            val traits = TypeTraits.Byte
            checkAll<Byte> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it.toInt() and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<Byte> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< Byte.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it.toInt() and (1 shl i) != 0)
                }
            }
            checkAll<Byte> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<Byte> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("UByte") {
            val traits = TypeTraits.UByte
            checkAll<UByte> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it.toInt() and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<UByte> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< UByte.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it.toInt() and (1 shl i) != 0)
                }
            }
            checkAll<UByte> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<UByte> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("Short") {
            val traits = TypeTraits.Short
            checkAll<Short> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it.toInt() and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<Short> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< Short.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it.toInt() and (1 shl i) != 0)
                }
            }
            checkAll<Short> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<Short> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("UShort") {
            val traits = TypeTraits.UShort
            checkAll<UShort> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it.toInt() and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<UShort> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< UShort.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it.toInt() and (1 shl i) != 0)
                }
            }
            checkAll<UShort> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<UShort> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("Int") {
            val traits = TypeTraits.Int
            checkAll<Int> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<Int> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< Int.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it and (1 shl i) != 0)
                }
            }
            checkAll<Int> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<Int> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("UInt") {
            val traits = TypeTraits.UInt
            checkAll<UInt> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it and (1U shl index) != 0U) shouldBeEqual bit
                }
            }
            checkAll<UInt> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< UInt.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it and (1U shl i) != 0U)
                }
            }
            checkAll<UInt> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<UInt> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("Long") {
            val traits = TypeTraits.Long
            checkAll<Long> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it and (1L shl index) != 0L) shouldBeEqual bit
                }
            }
            checkAll<Long> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< Long.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it and (1L shl i) != 0L)
                }
            }
            checkAll<Long> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<Long> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("ULong") {
            val traits = TypeTraits.ULong
            checkAll<ULong> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it and (1UL shl index) != 0UL) shouldBeEqual bit
                }
            }
            checkAll<ULong> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< ULong.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it and (1UL shl i) != 0UL)
                }
            }
            checkAll<ULong> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<ULong> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }
    }
}
