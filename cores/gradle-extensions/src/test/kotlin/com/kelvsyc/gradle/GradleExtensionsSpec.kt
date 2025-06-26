package com.kelvsyc.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.invocation.Gradle

class GradleExtensionsSpec : FunSpec() {
    init {
        test("root Gradle") {
            val root = mockk<Gradle>()
            every { root.parent } returns null

            val result = root.rootGradle

            result shouldBeSameInstanceAs root
        }

        test("parent Gradle") {
            val root = mockk<Gradle>()
            every { root.parent } returns null
            val child = mockk<Gradle>()
            every { child.parent } returns root

            val result = child.rootGradle

            result shouldBeSameInstanceAs root
        }
    }
}
