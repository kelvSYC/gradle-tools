package com.kelvsyc.kotlin.core.fp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.checkAll
import kotlin.math.absoluteValue

class DoubleDoubleAdditionSpec : FunSpec() {
    init {
        test("fastTwoSum") {
            val arbA = Arb.double()
            val arbB = arbA.flatMap { a ->
                Arb.double().filter { it.absoluteValue <= a.absoluteValue }
            }
            checkAll(arbA, arbB) { a, b ->
                val result = DoubleDouble.Addition.fastTwoSum(a, b)

                result.high shouldBeEqual (a + b)
                // FIXME How do we verify result.low? There appears to be cases where
                //       result.low.absoluteValue == result.high.ulp
            }
        }

        test("twoSum") {
            checkAll<Double, Double> { a, b ->
                val result = DoubleDouble.Addition.twoSum(a, b)

                result.toFloatingPoint() shouldBeEqual (a + b)
                // FIXME Probably needs further validation wrt precision
            }
        }

        test("growExpansion") {
            checkAll<Double, Double, Double> { a1, a2, b ->
                val a = DoubleDouble.Addition.twoSum(a1, a2)

                val result = DoubleDouble.Addition.growExpansion(listOf(a.low, a.high), b)

                result.sum() shouldBeEqual DoubleDouble.Addition.twoSum(a, b).toFloatingPoint()
                // FIXME Probably needs further validation wrt precision
            }
        }

        test("expansionSum") {
            checkAll<Double, Double, Double, Double> { a1, a2, b1, b2 ->
                val a = DoubleDouble.Addition.twoSum(a1, a2)
                val b = DoubleDouble.Addition.twoSum(b1, b2)

                val result = DoubleDouble.Addition.expansionSum(listOf(a.low, a.high), listOf(b.low, b.high))

                result.sum() shouldBeEqual DoubleDouble.Addition.twoSum(a, b).toFloatingPoint()
                // FIXME Probably needs further validation wrt precision
            }
        }
    }
}
