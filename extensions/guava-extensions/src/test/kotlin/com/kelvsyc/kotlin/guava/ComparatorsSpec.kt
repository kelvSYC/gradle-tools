package com.kelvsyc.kotlin.guava

import com.google.common.primitives.SignedBytes
import com.google.common.primitives.UnsignedBytes
import com.google.common.primitives.UnsignedInts
import com.google.common.primitives.UnsignedLongs
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class ComparatorsSpec : FunSpec() {
    init {
        test("signedByteComparator") {
            checkAll<Byte, Byte> { a, b ->
                val actual = Comparators.signedByteComparator.compare(a, b)
                val expected = SignedBytes.compare(a, b)

                actual shouldBeEqual expected
            }
        }

        test("unsignedByteComparator") {
            checkAll<Byte, Byte> { a, b ->
                val actual = Comparators.unsignedByteComparator.compare(a, b)
                val expected = UnsignedBytes.compare(a, b)

                actual shouldBeEqual expected
            }
        }

        test("unsignedIntComparator") {
            checkAll<Int, Int> { a, b ->
                val actual = Comparators.unsignedIntComparator.compare(a, b)
                val expected = UnsignedInts.compare(a, b)

                actual shouldBeEqual expected
            }
        }

        test("unsignedLongComparator") {
            checkAll<Long, Long> { a, b ->
                val actual = Comparators.unsignedLongComparator.compare(a, b)
                val expected = UnsignedLongs.compare(a, b)

                actual shouldBeEqual expected
            }
        }
    }
}
