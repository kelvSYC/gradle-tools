package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.short
import io.kotest.property.arbitrary.uByte
import io.kotest.property.arbitrary.uInt
import io.kotest.property.arbitrary.uLong
import io.kotest.property.arbitrary.uShort
import io.kotest.property.checkAll
import kotlin.math.sign

class CeilDivSpec : FunSpec() {
    init {
        context("Byte") {
            val lhsArb = Arb.byte()
            test("Byte") {
                val rhsArb = Arb.byte().filter { it.toInt() != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.toInt().sign != rhs.toInt().sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Short") {
                val rhsArb = Arb.short().filter { it.toInt() != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.toInt().sign != rhs.toInt().sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Int") {
                val rhsArb = Arb.int().filter { it != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.toInt().sign != rhs.sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Long") {
                val rhsArb = Arb.long().filter { it != 0L }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.toInt().sign != rhs.sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0L) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
        }

        context("UByte") {
            val lhsArb = Arb.uByte()
            test("UByte") {
                val rhsArb = Arb.uByte().filter { !TypeTraits.UByte.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0U) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("UShort") {
                val rhsArb = Arb.uShort().filter { !TypeTraits.UShort.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0U) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("UInt") {
                val rhsArb = Arb.uInt().filter { !TypeTraits.UInt.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0U) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("ULong") {
                val rhsArb = Arb.uLong().filter { !TypeTraits.ULong.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0UL) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
        }

        context("Short") {
            val lhsArb = Arb.short()
            test("Byte") {
                val rhsArb = Arb.byte().filter { it.toInt() != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.toInt().sign != rhs.toInt().sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Short") {
                val rhsArb = Arb.short().filter { it.toInt() != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.toInt().sign != rhs.toInt().sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Int") {
                val rhsArb = Arb.int().filter { it != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.toInt().sign != rhs.sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Long") {
                val rhsArb = Arb.long().filter { it != 0L }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.toInt().sign != rhs.sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0L) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
        }

        context("UShort") {
            val lhsArb = Arb.uShort()
            test("UByte") {
                val rhsArb = Arb.uByte().filter { !TypeTraits.UByte.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0U) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("UShort") {
                val rhsArb = Arb.uShort().filter { !TypeTraits.UShort.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0U) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("UInt") {
                val rhsArb = Arb.uInt().filter { !TypeTraits.UInt.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0U) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("ULong") {
                val rhsArb = Arb.uLong().filter { !TypeTraits.ULong.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0UL) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
        }

        context("Int") {
            val lhsArb = Arb.int()
            test("Byte") {
                val rhsArb = Arb.byte().filter { it.toInt() != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.sign != rhs.toInt().sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Short") {
                val rhsArb = Arb.short().filter { it.toInt() != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.sign != rhs.toInt().sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Int") {
                val rhsArb = Arb.int().filter { it != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.sign != rhs.sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Long") {
                val rhsArb = Arb.long().filter { it != 0L }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.sign != rhs.sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0L) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
        }

        context("UInt") {
            val lhsArb = Arb.uInt()
            test("UByte") {
                val rhsArb = Arb.uByte().filter { !TypeTraits.UByte.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0U) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("UShort") {
                val rhsArb = Arb.uShort().filter { !TypeTraits.UShort.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0U) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("UInt") {
                val rhsArb = Arb.uInt().filter { !TypeTraits.UInt.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0U) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("ULong") {
                val rhsArb = Arb.uLong().filter { !TypeTraits.ULong.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0UL) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
        }

        context("Long") {
            val lhsArb = Arb.long()
            test("Byte") {
                val rhsArb = Arb.byte().filter { it.toInt() != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.sign != rhs.toInt().sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0L) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Short") {
                val rhsArb = Arb.short().filter { it.toInt() != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.sign != rhs.toInt().sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0L) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Int") {
                val rhsArb = Arb.int().filter { it != 0 }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.sign != rhs.sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0L) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
            test("Long") {
                val rhsArb = Arb.long().filter { it != 0L }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs.sign != rhs.sign) {
                        result shouldBeEqual (lhs / rhs)
                    } else if (lhs % rhs == 0L) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1
                    }
                }
            }
        }

        context("ULong") {
            val lhsArb = Arb.uLong()
            test("UByte") {
                val rhsArb = Arb.uByte().filter { !TypeTraits.UByte.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0UL) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("UShort") {
                val rhsArb = Arb.uShort().filter { !TypeTraits.UShort.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0UL) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("UInt") {
                val rhsArb = Arb.uInt().filter { !TypeTraits.UInt.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0UL) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
            test("ULong") {
                val rhsArb = Arb.uLong().filter { !TypeTraits.ULong.isZero(it) }
                checkAll(lhsArb, rhsArb) { lhs, rhs ->
                    val result = lhs.ceilDiv(rhs)

                    if (lhs % rhs == 0UL) {
                        result shouldBeEqual (lhs / rhs)
                    } else {
                        result shouldBeEqual (lhs / rhs) + 1U
                    }
                }
            }
        }
    }
}
