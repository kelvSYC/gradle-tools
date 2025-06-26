package com.kelvsyc.kotlin.commons.lang.reflect

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual

class ReflectedStaticFieldDelegateSpec : FunSpec() {
    @Suppress("detekt:UtilityClassWithPublicConstructor")
    class Dummy {
        companion object {
            @JvmField
            var field: Int = 0
        }
    }

    init {
        test("Simple Read") {
            @Suppress("detekt:VariableNaming")
            val SIZE: Int by ReflectedStaticFieldDelegate(Integer::class.java)

            SIZE shouldBeEqual Int.SIZE_BITS
        }

        test("Simple Write") {
            var field: Int by ReflectedStaticFieldDelegate(Dummy::class.java)
            field = 1

            Dummy.field shouldBeEqual 1
        }
    }
}
