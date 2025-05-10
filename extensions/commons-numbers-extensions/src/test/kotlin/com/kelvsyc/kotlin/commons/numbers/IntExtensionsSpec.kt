package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.numbers.core.DD

class IntExtensionsSpec : FunSpec() {
    init {
        test("toDD") {
            mockkStatic(DD::class) {
                val value = 1
                value.toDD()
                verify {
                    DD.of(value)
                }
            }
        }
    }
}
