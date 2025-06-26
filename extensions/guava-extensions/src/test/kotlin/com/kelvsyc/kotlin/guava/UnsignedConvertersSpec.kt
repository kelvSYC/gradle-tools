package com.kelvsyc.kotlin.guava

import com.google.common.primitives.UnsignedInteger
import com.google.common.primitives.UnsignedLong
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

class UnsignedConvertersSpec : FunSpec() {
    init {
        test("int") {
            checkAll<UnsignedInteger>(Arb.int().map(UnsignedInteger::fromIntBits)) {
                UnsignedIntegerConverter.convert(it) shouldBeEqual it.asUInt
            }
            checkAll<UInt> {
                UnsignedIntegerConverter.reverse().convert(it) shouldBeEqual it.asGuavaUnsignedInteger
            }
        }

        test("long") {
            checkAll<UnsignedLong>(Arb.long().map(UnsignedLong::fromLongBits)) {
                UnsignedLongConverter.convert(it) shouldBeEqual it.asULong
            }
            checkAll<ULong> {
                UnsignedLongConverter.reverse().convert(it) shouldBeEqual it.asGuavaUnsignedLong
            }
        }
    }
}
