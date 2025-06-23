package com.kelvsyc.kotlin.guava.collect

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import java.math.BigInteger

class DiscreteDomainsSpec : FunSpec() {
    init {
        test("Short extremes") {
            DiscreteDomains.ShortDomain.maxValue() shouldBeEqual Short.MAX_VALUE
            DiscreteDomains.ShortDomain.minValue() shouldBeEqual Short.MIN_VALUE
        }

        test("Short distance") {
            checkAll<Short, Short> { a, b ->
                DiscreteDomains.ShortDomain.distance(a, b) shouldBeEqual b.toLong() - a.toLong()
            }
        }

        test("Short next") {
            checkAll<Short> {
                val expected = it.takeIf { it != Short.MAX_VALUE }?.inc()
                DiscreteDomains.ShortDomain.next(it) shouldBe expected
            }
        }

        test("Short previous") {
            checkAll<Short> {
                val expected = it.takeIf { it != Short.MIN_VALUE }?.dec()
                DiscreteDomains.ShortDomain.previous(it) shouldBe expected
            }
        }

        test("UShort extremes") {
            DiscreteDomains.UShortDomain.maxValue() shouldBeEqual UShort.MAX_VALUE
            DiscreteDomains.UShortDomain.minValue() shouldBeEqual UShort.MIN_VALUE
        }

        test("UShort distance") {
            checkAll<UShort, UShort> { a, b ->
                DiscreteDomains.UShortDomain.distance(a, b) shouldBeEqual b.toLong() - a.toLong()
            }
        }

        test("UShort next") {
            checkAll<UShort> {
                val expected = it.takeIf { it != UShort.MAX_VALUE }?.inc()
                DiscreteDomains.UShortDomain.next(it) shouldBe expected
            }
        }

        test("UShort previous") {
            checkAll<UShort> {
                val expected = it.takeIf { it != UShort.MIN_VALUE }?.dec()
                DiscreteDomains.UShortDomain.previous(it) shouldBe expected
            }
        }

        test("UInt extremes") {
            DiscreteDomains.UIntDomain.maxValue() shouldBeEqual UInt.MAX_VALUE
            DiscreteDomains.UIntDomain.minValue() shouldBeEqual UInt.MIN_VALUE
        }

        test("UInt distance") {
            checkAll<UInt, UInt> { a, b ->
                DiscreteDomains.UIntDomain.distance(a, b) shouldBeEqual b.toLong() - a.toLong()
            }
        }

        test("UInt next") {
            checkAll<UInt> {
                val expected = it.takeIf { it != UInt.MAX_VALUE }?.inc()
                DiscreteDomains.UIntDomain.next(it) shouldBe expected
            }
        }

        test("UInt previous") {
            checkAll<UInt> {
                val expected = it.takeIf { it != UInt.MIN_VALUE }?.dec()
                DiscreteDomains.UIntDomain.previous(it) shouldBe expected
            }
        }

        test("ULong extremes") {
            DiscreteDomains.ULongDomain.maxValue() shouldBeEqual ULong.MAX_VALUE
            DiscreteDomains.ULongDomain.minValue() shouldBeEqual ULong.MIN_VALUE
        }

        test("ULong distance") {
            checkAll<ULong, ULong> { a, b ->
                val bigA = a.let { value ->
                    val result = ByteArray(ULong.SIZE_BYTES)
                    (0 ..< ULong.SIZE_BYTES).forEach {
                        result[ULong.SIZE_BYTES - it - 1] = (value shr (Byte.SIZE_BITS * (it))).toByte()
                    }
                    BigInteger(1, result)
                }
                val bigB = b.let { value ->
                    val result = ByteArray(ULong.SIZE_BYTES)
                    (0 ..< ULong.SIZE_BYTES).forEach {
                        result[ULong.SIZE_BYTES - it - 1] = (value shr (Byte.SIZE_BITS * it)).toByte()
                    }
                    BigInteger(1, result)
                }
                val expected = (bigB - bigA).coerceIn(Long.MIN_VALUE.toBigInteger(), Long.MAX_VALUE.toBigInteger()).toLong()
                DiscreteDomains.ULongDomain.distance(a, b) shouldBeEqual expected
            }
        }

        test("ULong next") {
            checkAll<ULong> {
                val expected = it.takeIf { it != ULong.MAX_VALUE }?.inc()
                DiscreteDomains.ULongDomain.next(it) shouldBe expected
            }
        }

        test("ULong previous") {
            checkAll<ULong> {
                val expected = it.takeIf { it != ULong.MIN_VALUE }?.dec()
                DiscreteDomains.ULongDomain.previous(it) shouldBe expected
            }
        }
    }
}
