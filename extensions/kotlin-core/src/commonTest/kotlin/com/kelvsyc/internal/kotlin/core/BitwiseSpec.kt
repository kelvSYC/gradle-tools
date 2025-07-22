package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

class BitwiseSpec : FunSpec() {
    init {
        test("Byte") {
            val traits = TypeTraits.Byte
            checkAll<Byte, Byte> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<Byte, Byte> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<Byte, Byte> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<Byte> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("UByte") {
            val traits = TypeTraits.UByte
            checkAll<UByte, UByte> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<UByte, UByte> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<UByte, UByte> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<UByte> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Short") {
            val traits = TypeTraits.Short
            checkAll<Short, Short> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<Short, Short> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<Short, Short> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<Short> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("UShort") {
            val traits = TypeTraits.UShort
            checkAll<UShort, UShort> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<UShort, UShort> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<UShort, UShort> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<UShort> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Int") {
            val traits = TypeTraits.Int
            checkAll<Int, Int> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<Int, Int> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<Int, Int> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<Int> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("UInt") {
            val traits = TypeTraits.UInt
            checkAll<UInt, UInt> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<UInt, UInt> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<UInt, UInt> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<UInt> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Long") {
            val traits = TypeTraits.Long
            checkAll<Long, Long> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<Long, Long> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<Long, Long> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<Long> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("ULong") {
            val traits = TypeTraits.ULong
            checkAll<ULong, ULong> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<ULong, ULong> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<ULong, ULong> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<ULong> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }
    }
}
