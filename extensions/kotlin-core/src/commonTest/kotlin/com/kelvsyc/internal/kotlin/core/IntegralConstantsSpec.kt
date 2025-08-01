package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.property.checkAll

class IntegralConstantsSpec : FunSpec() {
    init {
        context("Byte") {
            val traits = TypeTraits.Byte
            test("zero") {
                traits.isZero(traits.zero).shouldBeTrue()
            }
            test("minValue") {
                checkAll<Byte> {
                    it > traits.minValue
                }
            }
            test("maxValue") {
                checkAll<Byte> {
                    it < traits.maxValue
                }
            }
        }

        context("UByte") {
            val traits = TypeTraits.UByte
            test("zero") {
                traits.isZero(traits.zero).shouldBeTrue()
            }
            test("minValue") {
                checkAll<UByte> {
                    it > traits.minValue
                }
            }
            test("maxValue") {
                checkAll<UByte> {
                    it < traits.maxValue
                }
            }
        }

        context("Short") {
            val traits = TypeTraits.Short
            test("zero") {
                traits.isZero(traits.zero).shouldBeTrue()
            }
            test("minValue") {
                checkAll<Short> {
                    it > traits.minValue
                }
            }
            test("maxValue") {
                checkAll<Short> {
                    it < traits.maxValue
                }
            }
        }

        context("UShort") {
            val traits = TypeTraits.UShort
            test("zero") {
                traits.isZero(traits.zero).shouldBeTrue()
            }
            test("minValue") {
                checkAll<UShort> {
                    it > traits.minValue
                }
            }
            test("maxValue") {
                checkAll<UShort> {
                    it < traits.maxValue
                }
            }
        }

        context("Int") {
            val traits = TypeTraits.Int
            test("zero") {
                traits.isZero(traits.zero).shouldBeTrue()
            }
            test("minValue") {
                checkAll<Int> {
                    it > traits.minValue
                }
            }
            test("maxValue") {
                checkAll<Int> {
                    it < traits.maxValue
                }
            }
        }

        context("UInt") {
            val traits = TypeTraits.UInt
            test("zero") {
                traits.isZero(traits.zero).shouldBeTrue()
            }
            test("minValue") {
                checkAll<UInt> {
                    it > traits.minValue
                }
            }
            test("maxValue") {
                checkAll<UInt> {
                    it < traits.maxValue
                }
            }
        }

        context("Long") {
            val traits = TypeTraits.Long
            test("zero") {
                traits.isZero(traits.zero).shouldBeTrue()
            }
            test("minValue") {
                checkAll<Long> {
                    it > traits.minValue
                }
            }
            test("maxValue") {
                checkAll<Long> {
                    it < traits.maxValue
                }
            }
        }

        context("ULong") {
            val traits = TypeTraits.ULong
            test("zero") {
                traits.isZero(traits.zero).shouldBeTrue()
            }
            test("minValue") {
                checkAll<ULong> {
                    it > traits.minValue
                }
            }
            test("maxValue") {
                checkAll<ULong> {
                    it < traits.maxValue
                }
            }
        }
    }
}
