package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.short
import io.kotest.property.checkAll
import kotlin.math.sign

class CeilDivSpec : FunSpec() {
    init {
        test("Byte/Byte") {
            val lhsArb = Arb.byte()
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

        test("Byte/Short") {
            val lhsArb = Arb.byte()
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

        test("Byte/Int") {
            val lhsArb = Arb.byte()
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

        test("Byte/Long") {
            val lhsArb = Arb.byte()
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

        test("Short/Byte") {
            val lhsArb = Arb.short()
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

        test("Short/Short") {
            val lhsArb = Arb.short()
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

        test("Short/Int") {
            val lhsArb = Arb.short()
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

        test("Short/Long") {
            val lhsArb = Arb.short()
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

        test("Int/Byte") {
            val lhsArb = Arb.int()
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

        test("Int/Short") {
            val lhsArb = Arb.int()
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

        test("Int/Int") {
            val lhsArb = Arb.int()
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

        test("Int/Long") {
            val lhsArb = Arb.int()
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

        test("Long/Byte") {
            val lhsArb = Arb.long()
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

        test("Long/Short") {
            val lhsArb = Arb.long()
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

        test("Long/Int") {
            val lhsArb = Arb.long()
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

        test("Long/Long") {
            val lhsArb = Arb.long()
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
}
