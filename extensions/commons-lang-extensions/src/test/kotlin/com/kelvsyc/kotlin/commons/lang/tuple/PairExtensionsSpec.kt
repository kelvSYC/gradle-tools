package com.kelvsyc.kotlin.commons.lang.tuple

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.apache.commons.lang3.tuple.Pair as CommonsPair
import org.apache.commons.lang3.tuple.Triple as CommonsTriple

class PairExtensionsSpec : FunSpec() {
    init {
        test("Kotlin to Commons Pairs") {
            val first = Any()
            val second = Any()
            val value = first to second
            val commonsValue = value.toCommonsPair()

            commonsValue.left shouldBeSameInstanceAs first
            commonsValue.right shouldBeSameInstanceAs second
        }

        test("Commons to Kotlin Pairs") {
            val first = Any()
            val second = Any()
            val value = CommonsPair.of(first, second)
            val kotlinValue = value.toKotlinPair()

            kotlinValue.first shouldBeSameInstanceAs first
            kotlinValue.second shouldBeSameInstanceAs second
        }

        test("Kotlin to Commons Triples") {
            val first = Any()
            val second = Any()
            val third = Any()
            val value = Triple(first, second, third)
            val commonsValue = value.toCommonsTriple()

            commonsValue.left shouldBeSameInstanceAs first
            commonsValue.middle shouldBeSameInstanceAs second
            commonsValue.right shouldBeSameInstanceAs third
        }

        test("Commons to Kotlin Triples") {
            val first = Any()
            val second = Any()
            val third = Any()
            val value = CommonsTriple.of(first, second, third)
            val kotlinValue = value.toKotlinTriple()

            kotlinValue.first shouldBeSameInstanceAs first
            kotlinValue.second shouldBeSameInstanceAs second
            kotlinValue.third shouldBeSameInstanceAs third
        }

        test("Commons Pair Destructure") {
            val first = Any()
            val second = Any()
            val value = CommonsPair.of(first, second)
            val (expectedFirst, expectedSecond) = value

            value.left shouldBeSameInstanceAs first
            value.right shouldBeSameInstanceAs second
        }

        test("Commons Triple Destructure") {
            val first = Any()
            val second = Any()
            val third = Any()
            val value = CommonsTriple.of(first, second, third)
            val (expectedFirst, expectedSecond, expectedThird) = value

            value.left shouldBeSameInstanceAs first
            value.middle shouldBeSameInstanceAs second
            value.right shouldBeSameInstanceAs third
        }
    }
}
