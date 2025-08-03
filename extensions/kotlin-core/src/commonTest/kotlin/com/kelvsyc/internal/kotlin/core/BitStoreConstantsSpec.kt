package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import com.kelvsyc.kotlin.core.traits.ArrayBitStoreConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.array
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.intArray
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.longArray
import io.kotest.property.arbitrary.short
import io.kotest.property.arbitrary.shortArray
import io.kotest.property.arbitrary.uByte
import io.kotest.property.arbitrary.uByteArray
import io.kotest.property.arbitrary.uInt
import io.kotest.property.arbitrary.uIntArray
import io.kotest.property.arbitrary.uLong
import io.kotest.property.arbitrary.uLongArray
import io.kotest.property.arbitrary.uShort
import io.kotest.property.arbitrary.uShortArray
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.constant

@OptIn(ExperimentalUnsignedTypes::class)
class BitStoreConstantsSpec : FunSpec() {
    init {
        context("Byte") {
            val traits = TypeTraits.Byte
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.byte().filter { !traits.isZero(it) }) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("UByte") {
            val traits = TypeTraits.UByte
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.uByte().filter { !traits.isZero(it) }) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("Short") {
            val traits = TypeTraits.Short
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.short().filter { !traits.isZero(it) }) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("UShort") {
            val traits = TypeTraits.UShort
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.uShort().filter { !traits.isZero(it) }) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("Int") {
            val traits = TypeTraits.Int
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.int().filter { !traits.isZero(it) }) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("UInt") {
            val traits = TypeTraits.UInt
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.uInt().filter { !traits.isZero(it) }) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("Long") {
            val traits = TypeTraits.Long
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.long().filter { !traits.isZero(it) }) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("ULong") {
            val traits = TypeTraits.ULong
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                checkAll(Arb.uLong().filter { !traits.isZero(it) }) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("ByteArray") {
            val traits = ArrayBitStoreConstants.ofByteArray(2)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                val arb = Arb.byteArray(Exhaustive.constant(2), Arb.byte().filter { !TypeTraits.Byte.isZero(it) })
                checkAll(arb) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("UByteArray") {
            val traits = ArrayBitStoreConstants.ofUByteArray(2)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                val arb = Arb.uByteArray(Exhaustive.constant(2), Arb.uByte().filter { !TypeTraits.UByte.isZero(it) })
                checkAll(arb) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("ShortArray") {
            val traits = ArrayBitStoreConstants.ofShortArray(2)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                val arb = Arb.shortArray(Exhaustive.constant(2), Arb.short().filter { !TypeTraits.Short.isZero(it) })
                checkAll(arb) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("UShortArray") {
            val traits = ArrayBitStoreConstants.ofUShortArray(2)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                val arb = Arb.uShortArray(Exhaustive.constant(2), Arb.uShort().filter { !TypeTraits.UShort.isZero(it) })
                checkAll(arb) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("IntArray") {
            val traits = ArrayBitStoreConstants.ofIntArray(2)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                val arb = Arb.intArray(Exhaustive.constant(2), Arb.int().filter { !TypeTraits.Int.isZero(it) })
                checkAll(arb) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("UIntArray") {
            val traits = ArrayBitStoreConstants.ofUIntArray(2)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                val arb = Arb.uIntArray(Exhaustive.constant(2), Arb.uInt().filter { !TypeTraits.UInt.isZero(it) })
                checkAll(arb) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("LongArray") {
            val traits = ArrayBitStoreConstants.ofLongArray(2)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                val arb = Arb.longArray(Exhaustive.constant(2), Arb.long().filter { !TypeTraits.Long.isZero(it) })
                checkAll(arb) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("ULongArray") {
            val traits = ArrayBitStoreConstants.ofULongArray(2)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                val arb = Arb.uLongArray(Exhaustive.constant(2), Arb.uLong().filter { !TypeTraits.ULong.isZero(it) })
                checkAll(arb) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }

        context("Object array") {
            val traits = ArrayBitStoreConstants.ofObjectArray(2, TypeTraits.Byte, TypeTraits.Byte)
            test("allClear") {
                traits.isAllClear(traits.allClear).shouldBeTrue()
                traits.hasSetBits(traits.allClear).shouldBeFalse()
            }
            test("hasSetBits") {
                val arb = Arb.array(Arb.byte().filter { !TypeTraits.Byte.isZero(it) }, 2..2)
                checkAll(arb) {
                    traits.hasSetBits(it).shouldBeTrue()
                    traits.isAllClear(it).shouldBeFalse()
                }
            }
        }
    }
}
