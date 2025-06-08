package com.kelvsyc.kotlin.commons.lang.reflect

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeSameInstanceAs

class ReflectedFieldDelegateSpec : FunSpec() {
    class Dummy {
        @JvmField
        val readable = Any()

        @JvmField
        var writable = 0
    }

    init {
        test("Simple Read") {
            val value = Dummy()
            val readable: Any by ReflectedFieldDelegate(value)

            readable shouldBeSameInstanceAs value.readable
        }

        test("Simple Write") {
            val value = Dummy()
            var writable: Int by ReflectedFieldDelegate(value)
            writable = 1

            writable shouldBeEqual value.writable
        }
    }
}
