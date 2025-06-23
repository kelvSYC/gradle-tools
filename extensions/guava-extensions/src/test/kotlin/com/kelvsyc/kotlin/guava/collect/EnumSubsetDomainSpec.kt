package com.kelvsyc.kotlin.guava.collect

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum

class EnumSubsetDomainSpec : FunSpec() {
    enum class Dummy { FOO, BAR, BAZ }
    val genDummySet = Arb.set(Exhaustive.enum<Dummy>(), 0.. Dummy.entries.size)

    init {
        test("extremes") {
            val domain = EnumSubsetDomain.of<Dummy>()

            domain.maxValue() shouldContainAll Dummy.entries
            domain.minValue() shouldNotContainAnyOf Dummy.entries
        }

        test("next") {
            val domain = EnumSubsetDomain.of<Dummy>()

            checkAll(genDummySet) {
                val source = EnumSubset.of(it)
                val bitValue = it.let {
                    var result = 0
                    if (it.contains(Dummy.FOO)) result = result or 1
                    if (it.contains(Dummy.BAR)) result = result or 2
                    if (it.contains(Dummy.BAZ)) result = result or 4
                    result
                }

                val result = domain.next(source)
                val resultBitValue = result?.let {
                    var result = 0
                    if (it.contains(Dummy.FOO)) result = result or 1
                    if (it.contains(Dummy.BAR)) result = result or 2
                    if (it.contains(Dummy.BAZ)) result = result or 4
                    result
                }

                if (bitValue == 7) {
                    resultBitValue shouldBe null
                } else {
                    resultBitValue shouldBe bitValue + 1
                }
            }
        }

        test("previous") {
            val domain = EnumSubsetDomain.of<Dummy>()

            checkAll(genDummySet) {
                val source = EnumSubset.of(it)
                val bitValue = it.let {
                    var result = 0
                    if (it.contains(Dummy.FOO)) result = result or 1
                    if (it.contains(Dummy.BAR)) result = result or 2
                    if (it.contains(Dummy.BAZ)) result = result or 4
                    result
                }

                val result = domain.previous(source)
                val resultBitValue = result?.let {
                    var result = 0
                    if (it.contains(Dummy.FOO)) result = result or 1
                    if (it.contains(Dummy.BAR)) result = result or 2
                    if (it.contains(Dummy.BAZ)) result = result or 4
                    result
                }

                if (bitValue == 0) {
                    resultBitValue shouldBe null
                } else {
                    resultBitValue shouldBe bitValue - 1
                }
            }
        }

        test("distance") {
            val domain = EnumSubsetDomain.of<Dummy>()

            checkAll(genDummySet, genDummySet) { a, b ->
                val sourceA = EnumSubset.of(a)
                val sourceB = EnumSubset.of(b)
                val bitValueA = a.let {
                    var result = 0
                    if (it.contains(Dummy.FOO)) result = result or 1
                    if (it.contains(Dummy.BAR)) result = result or 2
                    if (it.contains(Dummy.BAZ)) result = result or 4
                    result
                }
                val bitValueB = b.let {
                    var result = 0
                    if (it.contains(Dummy.FOO)) result = result or 1
                    if (it.contains(Dummy.BAR)) result = result or 2
                    if (it.contains(Dummy.BAZ)) result = result or 4
                    result
                }

                val result = domain.distance(sourceA, sourceB)

                result shouldBeEqual (bitValueB - bitValueA).toLong()
            }
        }
    }
}
