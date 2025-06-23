package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.Comparators
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import java.util.EnumSet

class EnumSubsetSpec : FunSpec() {
    enum class Dummy { FOO, BAR, BAZ }
    val genDummySet = Arb.set(Exhaustive.enum<Dummy>(), 0.. Dummy.entries.size)

    init {
        test("ofList") {
            checkAll (genDummySet) {
                val subset = EnumSubset.of(it)

                subset shouldContainExactly it
            }
        }

        test("ofVarargs") {
            checkAll(genDummySet) {
                val subset = EnumSubset.of(*it.toTypedArray())

                subset shouldContainExactly it
            }
        }

        test("ofEnumSet") {
            checkAll(genDummySet) {
                val source = if (it.isNotEmpty()) {
                    EnumSet.copyOf(it)
                } else {
                    EnumSet.noneOf(Dummy::class.java)
                }
                val subset = EnumSubset.of(source)

                subset shouldContainExactly it
            }
        }

        test("compare") {
            checkAll(genDummySet, genDummySet) { a, b ->
                val sourceA = if (a.isNotEmpty()) EnumSet.copyOf(a) else EnumSet.noneOf(Dummy::class.java)
                val sourceB = if (b.isNotEmpty()) EnumSet.copyOf(b) else EnumSet.noneOf(Dummy::class.java)
                val setA = EnumSubset.of(a)
                val setB = EnumSubset.of(b)

                val comparator = Comparators.lexicographical(Comparator.naturalOrder<Dummy>())

                val expected = comparator.compare(sourceA, sourceB)
                val actual = setA.compareTo(setB)

                when {
                    expected > 0 -> actual shouldBeGreaterThan 0
                    expected < 0 -> actual shouldBeLessThan 0
                    else -> actual shouldBeEqual 0
                }
            }
        }
    }
}
