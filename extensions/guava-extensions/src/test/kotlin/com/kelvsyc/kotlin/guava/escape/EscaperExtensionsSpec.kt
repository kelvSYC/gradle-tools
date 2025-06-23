package com.kelvsyc.kotlin.guava.escape

import com.google.common.escape.Escapers
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifySequence

class EscaperExtensionsSpec : FunSpec() {
    init {
        test("buildEscaper") {
            val builder = mockk<Escapers.Builder>(relaxed = true)
            val action = mockk<Escapers.Builder.() -> Unit>(relaxed = true)
            mockkStatic(Escapers::class) {
                every { Escapers.builder() } returns builder

                buildEscaper(action)

                verifySequence {
                    Escapers.builder()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }
    }
}
