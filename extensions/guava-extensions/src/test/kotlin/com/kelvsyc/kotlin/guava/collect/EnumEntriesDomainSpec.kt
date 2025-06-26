package com.kelvsyc.kotlin.guava.collect

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class EnumEntriesDomainSpec : FunSpec() {
    enum class Dummy { FOO, BAR }

    init {
        test("extremes") {
            val domain = EnumEntriesDomain.of<Dummy>()

            domain.maxValue() shouldBeEqual Dummy.BAR
            domain.minValue() shouldBeEqual Dummy.FOO
        }

        test("next") {
            val domain = EnumEntriesDomain.of<Dummy>()

            domain.next(Dummy.FOO) shouldBe Dummy.BAR
            domain.next(Dummy.BAR) shouldBe null
        }

        test("previous") {
            val domain = EnumEntriesDomain.of<Dummy>()

            domain.previous(Dummy.FOO) shouldBe null
            domain.previous(Dummy.BAR) shouldBe Dummy.FOO
        }

        test("distance") {
            val domain = EnumEntriesDomain.of<Dummy>()

            checkAll<Dummy, Dummy> { a, b ->
                domain.distance(a, b) shouldBeEqual (b.ordinal - a.ordinal).toLong()
            }
        }
    }
}
