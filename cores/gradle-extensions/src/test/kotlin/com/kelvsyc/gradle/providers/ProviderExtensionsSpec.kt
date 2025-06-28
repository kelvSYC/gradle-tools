package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import org.gradle.testfixtures.ProjectBuilder

class ProviderExtensionsSpec : FunSpec() {
    init {
        test("cached") {
            val project = ProjectBuilder.builder().build()
            val input = listOf(1, 2).iterator()
            val provider = project.providers.provider {
                input.next()
            }

            val result = provider.cached(project.objects)

            result.get() shouldBeEqual 1
            result.get() shouldBeEqual 1
            provider.get() shouldBeEqual 2
        }

        test("cached list") {
            val project = ProjectBuilder.builder().build()
            val input = listOf(listOf(1), listOf(2)).iterator()
            val provider = project.providers.provider {
                input.next()
            }

            val result = provider.cached(project.objects)

            result.get() shouldBeEqual listOf(1)
            result.get() shouldBeEqual listOf(1)
            provider.get() shouldBeEqual listOf(2)
            result.get() shouldBeEqual listOf(1)
        }

        test("cached set") {
            val project = ProjectBuilder.builder().build()
            val input = listOf(setOf(1), setOf(2)).iterator()
            val provider = project.providers.provider {
                input.next()
            }

            val result = provider.cached(project.objects)

            result.get() shouldBeEqual setOf(1)
            result.get() shouldBeEqual setOf(1)
            provider.get() shouldBeEqual setOf(2)
            result.get() shouldBeEqual setOf(1)
        }

        test("cached map") {
            val project = ProjectBuilder.builder().build()
            val input = listOf(mapOf(1 to 1), mapOf(2 to 2)).iterator()
            val provider = project.providers.provider {
                input.next()
            }

            val result = provider.cached(project.objects)

            result.get() shouldBeEqual mapOf(1 to 1)
            result.get() shouldBeEqual mapOf(1 to 1)
            provider.get() shouldBeEqual mapOf(2 to 2)
            result.get() shouldBeEqual mapOf(1 to 1)
        }
    }
}
